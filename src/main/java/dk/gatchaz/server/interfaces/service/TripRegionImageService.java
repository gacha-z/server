package dk.gatchaz.server.interfaces.service;

import dk.gatchaz.server.exception.CommonException;
import dk.gatchaz.server.interfaces.dto.LdongCode;
import dk.gatchaz.server.interfaces.dto.TourAreaBasedItem;
import dk.gatchaz.server.interfaces.dto.TripRegionImageFailure;
import dk.gatchaz.server.interfaces.dto.TripRegionImageResponse;
import dk.gatchaz.server.interfaces.dto.TripRegionImageTarget;
import dk.gatchaz.server.interfaces.mapper.TripRegionMapper;
import dk.gatchaz.server.interfaces.support.LdongCodeCache;
import dk.gatchaz.server.interfaces.support.TourApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * TourAPI 로 조회한 관광지의 대표 이미지를 trip_region.image_url 에 적재한다.
 * 매월 실행되는 배치(TripRegionImageScheduler)와 수동 실행 API 가 이 로직을 공유한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TripRegionImageService {

    /** 법정동 코드에서 시도 코드(앞 2자리) + 시군구 코드(3~5번째 자리)를 잘라 쓰므로 최소 5자리가 필요하다. */
    private static final int REGION_CODE_MIN_LENGTH = 5;
    private static final int REGN_CODE_END_INDEX = 2;
    private static final int SIGNGU_CODE_END_INDEX = 5;

    /** trip_region.image_url 컬럼 길이 */
    private static final int IMAGE_URL_MAX_LENGTH = 255;

    private static final String FAIL_NO_IMAGE = "대표 이미지가 있는 관광지를 찾지 못했습니다.";
    private static final String FAIL_IMAGE_URL_TOO_LONG = "대표 이미지 URL 이 저장 가능한 길이(255자)를 초과했습니다.";
    private static final String FAIL_API_ERROR = "TourAPI 조회에 실패했습니다.";
    private static final String FAIL_DB_ERROR = "image_url 저장에 실패했습니다.";
    private static final String FAIL_RATE_LIMITED = "TourAPI 초당 요청 제한을 초과했습니다. 잠시 후 다시 실행해 주세요.";
    private static final String FAIL_QUOTA_EXCEEDED = "TourAPI 일일 요청 한도를 초과했습니다. 다음 날 다시 실행해 주세요.";

    private final TripRegionMapper tripRegionMapper;
    private final TourApiClient tourApiClient;

    /**
     * 사용 중(use_yn = 'Y')인 모든 지역의 대표 이미지를 조회해 image_url 을 갱신한다.
     * 이미 값이 있는 지역도 새로 조회한 값으로 덮어쓴다. (TourAPI 이미지 URL 교체/삭제 반영)
     *
     * 지역마다 외부 API 를 호출하므로 전체를 하나의 트랜잭션으로 묶지 않는다.
     * 외부 호출 시간 동안 커넥션과 잠금을 점유하지 않기 위함이며,
     * 지역별 UPDATE 는 단일 문장이라 그 자체로 원자적이다.
     * 한 지역이 실패해도 나머지 지역은 계속 처리하고, 실패 내역은 결과에 담아 반환한다.
     */
    public TripRegionImageResponse updateRegionImages() {
        final List<TripRegionImageTarget> regions = tripRegionMapper.selectImageTargetRegions();
        // 법정동 코드 목록은 이 실행 동안만 재사용한다. (지역마다 다시 조회하지 않도록)
        final LdongCodeCache ldongCodes = new LdongCodeCache(tourApiClient);
        final List<TripRegionImageFailure> failures = new ArrayList<>();
        int successCount = 0;

        // 일일 한도를 넘기면 남은 지역도 모두 실패하므로 더 호출하지 않고 실패로만 기록한다.
        boolean quotaExceeded = false;

        for (final TripRegionImageTarget region : regions) {
            final String failReason = quotaExceeded ? FAIL_QUOTA_EXCEEDED : updateRegionImage(region, ldongCodes);
            if (failReason == null) {
                successCount++;
                continue;
            }
            if (FAIL_QUOTA_EXCEEDED.equals(failReason)) {
                quotaExceeded = true;
            }
            failures.add(new TripRegionImageFailure(
                    region.getTripRegionId(), region.getTripRegionName(), failReason));
        }

        return new TripRegionImageResponse(regions.size(), successCount, failures.size(), failures);
    }

    /**
     * 지역 1건의 대표 이미지를 조회해 갱신한다. 성공하면 null, 실패하면 사유 문자열을 반환한다.
     */
    private String updateRegionImage(final TripRegionImageTarget region, final LdongCodeCache ldongCodes) {
        final TourAreaBasedItem selected;
        try {
            selected = findRepresentativeItem(region, ldongCodes);
        } catch (final CommonException e) {
            log.warn("지역 대표 이미지 갱신 실패 - TourAPI 오류({}): tripRegionId={}, tripRegionName={}",
                    e.getErrorCode(), region.getTripRegionId(), region.getTripRegionName());
            return switch (e.getErrorCode()) {
                case EXTERNAL_API_QUOTA_EXCEEDED -> FAIL_QUOTA_EXCEEDED;
                case EXTERNAL_API_RATE_LIMITED -> FAIL_RATE_LIMITED;
                default -> FAIL_API_ERROR;
            };
        } catch (final Exception e) {
            log.warn("지역 대표 이미지 갱신 실패 - TourAPI 오류: tripRegionId={}, tripRegionName={}",
                    region.getTripRegionId(), region.getTripRegionName());
            return FAIL_API_ERROR;
        }

        if (selected == null) {
            log.warn("지역 대표 이미지 갱신 실패 - 대표 이미지 없음: tripRegionId={}, tripRegionName={}, tripRegionCode={}",
                    region.getTripRegionId(), region.getTripRegionName(), region.getTripRegionCode());
            return FAIL_NO_IMAGE;
        }

        final String imageUrl = selected.firstImage().strip();
        if (imageUrl.length() > IMAGE_URL_MAX_LENGTH) {
            log.warn("지역 대표 이미지 갱신 실패 - URL 길이 초과: tripRegionId={}, tripRegionName={}, length={}",
                    region.getTripRegionId(), region.getTripRegionName(), imageUrl.length());
            return FAIL_IMAGE_URL_TOO_LONG;
        }

        try {
            tripRegionMapper.updateImageUrl(region.getTripRegionId(), imageUrl);
        } catch (final Exception e) {
            log.warn("지역 대표 이미지 갱신 실패 - 저장 오류: tripRegionId={}, tripRegionName={}, message={}",
                    region.getTripRegionId(), region.getTripRegionName(), e.getMessage());
            return FAIL_DB_ERROR;
        }

        log.info("지역 대표 이미지 갱신: tripRegionId={}, tripRegionName={}, 관광지={}, imageUrl={}",
                region.getTripRegionId(), region.getTripRegionName(), selected.title(), imageUrl);
        return null;
    }

    /**
     * 지역의 대표 이미지로 쓸 관광지 1건을 찾는다. 찾지 못하면 null.
     *
     * 1. trip_region_code(법정동 10자리)를 잘라 조회한다. (대부분 이 경로에서 끝난다)
     * 2. 결과가 없으면 TourAPI 법정동 코드 목록에서 지역명으로 코드를 다시 찾아 조회한다.
     *    trip_region 의 코드와 TourAPI 의 코드가 어긋나는 지역들을 이 단계에서 구제한다.
     */
    private TourAreaBasedItem findRepresentativeItem(final TripRegionImageTarget region,
                                                     final LdongCodeCache ldongCodes) {
        final String regionCode = region.getTripRegionCode();
        if (isValidRegionCode(regionCode)) {
            final TourAreaBasedItem item = firstWithImage(tourApiClient.findAreaBasedItems(
                    regionCode.substring(0, REGN_CODE_END_INDEX),
                    regionCode.substring(REGN_CODE_END_INDEX, SIGNGU_CODE_END_INDEX)));
            if (item != null) {
                return item;
            }
        }
        return findByRegionName(region, ldongCodes);
    }

    /**
     * 지역명으로 TourAPI 법정동 코드를 찾아 관광지를 조회한다. 찾지 못하면 null.
     *
     * trip_region_name 은 "경기도 수원시" 처럼 "시도명 시군구명" 형태이며,
     * "세종특별자치시" 처럼 시군구가 나뉘지 않는 지역은 시도명만 있다.
     */
    private TourAreaBasedItem findByRegionName(final TripRegionImageTarget region, final LdongCodeCache ldongCodes) {
        final String regionName = region.getTripRegionName();
        if (!StringUtils.hasText(regionName)) {
            return null;
        }

        final int separatorIndex = regionName.indexOf(' ');
        final String sidoName = separatorIndex < 0 ? regionName : regionName.substring(0, separatorIndex);
        final String sigunguName = separatorIndex < 0 ? null : regionName.substring(separatorIndex + 1);

        final String regnCd = findCodeByName(ldongCodes.regnCodes(), sidoName);
        if (regnCd == null) {
            return null;
        }

        // 시군구가 나뉘지 않는 지역은 시도 코드로만 조회한다. (세종특별자치시는 시도 코드 자체가 36110 이다)
        if (sigunguName == null) {
            return firstWithImage(tourApiClient.findAreaBasedItems(regnCd, null));
        }

        final List<LdongCode> signguCodes = ldongCodes.signguCodes(regnCd);

        // 1. 시군구명이 정확히 일치하는 코드로 조회한다. (trip_region 의 시군구 코드가 TourAPI 와 다른 경우)
        final String signguCd = findCodeByName(signguCodes, sigunguName);
        if (signguCd != null) {
            final TourAreaBasedItem item = firstWithImage(tourApiClient.findAreaBasedItems(regnCd, signguCd));
            if (item != null) {
                return item;
            }
        }

        // 2. 관광지가 하위 일반구에만 등록된 경우를 처리한다. ("수원시" → "수원시 장안구", "수원시 권선구" ...)
        final String childPrefix = sigunguName + " ";
        for (final LdongCode child : signguCodes) {
            if (child.name().startsWith(childPrefix)) {
                final TourAreaBasedItem item =
                        firstWithImage(tourApiClient.findAreaBasedItems(regnCd, child.code()));
                if (item != null) {
                    return item;
                }
            }
        }
        return null;
    }

    /**
     * 조회된 순서대로 확인해 대표 이미지가 있는 첫 건을 반환한다.
     * (대표 이미지가 비어 있으면 다음 건으로 넘어간다) 없으면 null.
     */
    private TourAreaBasedItem firstWithImage(final List<TourAreaBasedItem> items) {
        return items.stream()
                .filter(item -> StringUtils.hasText(item.firstImage()))
                .findFirst()
                .orElse(null);
    }

    private String findCodeByName(final List<LdongCode> codes, final String name) {
        return codes.stream()
                .filter(code -> name.equals(code.name()))
                .map(LdongCode::code)
                .findFirst()
                .orElse(null);
    }

    /**
     * 법정동 코드가 시도(2자리) + 시군구(3자리)를 잘라 쓸 수 있는 숫자 문자열인지 확인한다.
     */
    private boolean isValidRegionCode(final String regionCode) {
        if (!StringUtils.hasText(regionCode) || regionCode.length() < REGION_CODE_MIN_LENGTH) {
            return false;
        }
        for (int i = 0; i < REGION_CODE_MIN_LENGTH; i++) {
            if (!Character.isDigit(regionCode.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
