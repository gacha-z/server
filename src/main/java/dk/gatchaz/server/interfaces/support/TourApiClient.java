package dk.gatchaz.server.interfaces.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.gatchaz.server.exception.CommonException;
import dk.gatchaz.server.exception.ErrorCode;
import dk.gatchaz.server.interfaces.config.TourApiProperties;
import dk.gatchaz.server.interfaces.dto.LdongCode;
import dk.gatchaz.server.interfaces.dto.TourAreaBasedItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 한국관광공사 국문 관광정보 서비스(TourAPI 4.0) 호출 담당.
 * 지역기반 관광정보 조회(areaBasedList2)와 법정동 코드 조회(ldongCode2)를 사용한다.
 */
@Slf4j
@Component
public class TourApiClient {

    private static final String AREA_BASED_LIST_PATH = "/areaBasedList2";
    private static final String LDONG_CODE_PATH = "/ldongCode2";

    /** 정렬 기준 Q = 대표이미지가 있는 항목만 수정일 최신순. 이미지가 없는 항목이 애초에 내려오지 않는다. */
    private static final String ARRANGE_WITH_IMAGE_MODIFIED_DESC = "Q";

    /** 법정동 코드 목록은 시도 16건, 시군구 최대 55건(경기도)이라 한 번에 모두 받는다. */
    private static final int LDONG_CODE_NUM_OF_ROWS = 500;

    private static final String RESPONSE_TYPE_JSON = "json";
    private static final String RESULT_CODE_SUCCESS = "0000";

    /** 초당 요청 제한 초과. 잠시 후 다시 호출하면 풀리므로 재시도 대상이다. */
    private static final String ERR_RATE_LIMIT_PER_SECOND = "LIMITED_NUMBER_OF_SERVICE_REQUESTS_PER_SECOND_EXCEEDS_ERROR";

    /** 요청 한도(일일) 초과. 재시도해도 풀리지 않으므로 즉시 실패시킨다. */
    private static final String ERR_QUOTA_EXCEEDED = "LIMITED_NUMBER_OF_SERVICE_REQUESTS_EXCEEDS_ERROR";

    /** 오류 응답 본문을 로그에 남길 때의 최대 길이 */
    private static final int ERROR_BODY_LOG_LIMIT = 500;

    private final TourApiProperties properties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    /** 호출 간격 제어용. 마지막 호출 시각을 공유하므로 동시 호출도 순서대로 간격을 지킨다. */
    private final Object throttleLock = new Object();
    private long lastRequestAt;

    public TourApiClient(final TourApiProperties properties,
                         final RestClient.Builder restClientBuilder,
                         final ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;

        final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()));
        requestFactory.setReadTimeout(Duration.ofMillis(properties.getReadTimeoutMs()));
        this.restClient = restClientBuilder.requestFactory(requestFactory).build();
    }

    /**
     * 법정동 시도 코드(lDongRegnCd)와 시군구 코드(lDongSignguCd)로 해당 지역의 관광지를 조회한다.
     * 시군구 코드가 비어 있으면 시도 전체를 조회한다. (세종특별자치시처럼 시군구가 나뉘지 않는 지역)
     * 대표 이미지가 있는 항목만 수정일 최신순으로 numOfRows 건까지 조회하며, 조회 결과가 없으면 빈 목록을 반환한다.
     * 호출 실패, 인증 오류, 응답 파싱 실패는 모두 EXTERNAL_API_ERROR 로 변환한다.
     */
    public List<TourAreaBasedItem> findAreaBasedItems(final String lDongRegnCd, final String lDongSignguCd) {
        final UriComponentsBuilder builder = baseBuilder(AREA_BASED_LIST_PATH)
                .queryParam("arrange", ARRANGE_WITH_IMAGE_MODIFIED_DESC)
                .queryParam("contentTypeId", properties.getContentTypeId())
                .queryParam("numOfRows", properties.getNumOfRows())
                .queryParam("lDongRegnCd", lDongRegnCd);
        if (StringUtils.hasText(lDongSignguCd)) {
            builder.queryParam("lDongSignguCd", lDongSignguCd);
        }
        final String context = "areaBasedList2 lDongRegnCd=" + lDongRegnCd + ", lDongSignguCd=" + lDongSignguCd;

        final List<JsonNode> nodes = readItems(request(builder.build(true).toUri(), context), context);
        final List<TourAreaBasedItem> items = new ArrayList<>(nodes.size());
        for (final JsonNode node : nodes) {
            items.add(new TourAreaBasedItem(
                    node.path("title").asText(""),
                    node.path("addr1").asText(""),
                    node.path("firstimage").asText("")));
        }
        return items;
    }

    /**
     * 법정동 코드 목록을 조회한다.
     * lDongRegnCd 가 비어 있으면 시도 목록을, 값이 있으면 해당 시도의 시군구 목록을 반환한다.
     */
    public List<LdongCode> findLdongCodes(final String lDongRegnCd) {
        final UriComponentsBuilder builder = baseBuilder(LDONG_CODE_PATH)
                .queryParam("numOfRows", LDONG_CODE_NUM_OF_ROWS);
        if (StringUtils.hasText(lDongRegnCd)) {
            builder.queryParam("lDongRegnCd", lDongRegnCd);
        }
        final String context = "ldongCode2 lDongRegnCd=" + lDongRegnCd;

        final List<JsonNode> nodes = readItems(request(builder.build(true).toUri(), context), context);
        final List<LdongCode> codes = new ArrayList<>(nodes.size());
        for (final JsonNode node : nodes) {
            codes.add(new LdongCode(node.path("code").asText(""), node.path("name").asText("")));
        }
        return codes;
    }

    /**
     * 서비스 키에 URL 예약 문자가 포함되어 있어 인코딩된 값으로 URI 를 조립한다.
     * (build(true) 는 이미 인코딩된 값으로 간주하므로 나머지 파라미터도 함께 인코딩한다.)
     */
    private UriComponentsBuilder baseBuilder(final String path) {
        return UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                .path(path)
                .queryParam("serviceKey", encode(properties.getServiceKey()))
                .queryParam("MobileOS", encode(properties.getMobileOs()))
                .queryParam("MobileApp", encode(properties.getMobileApp()))
                .queryParam("_type", RESPONSE_TYPE_JSON)
                .queryParam("pageNo", 1);
    }

    private String encode(final String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * TourAPI 를 호출한다. 초당 요청 제한(HTTP 429)에 걸리면 간격을 두고 재시도한다.
     * 일일 한도 초과는 재시도해도 풀리지 않으므로 즉시 실패시킨다.
     */
    private String request(final URI uri, final String context) {
        for (int attempt = 0; ; attempt++) {
            throttle();

            final String body;
            final String failureText;
            try {
                body = restClient.get()
                        .uri(uri)
                        .retrieve()
                        .body(String.class);
                // 게이트웨이가 HTTP 200 으로 오류 본문을 내려주는 경우가 있어 본문도 함께 확인한다.
                failureText = body;
            } catch (final Exception e) {
                // 요청 제한은 HTTP 429 로 내려오며, 예외 메시지에 응답 본문이 담겨 있다.
                final String message = e.getMessage();
                if (!contains(message, ERR_RATE_LIMIT_PER_SECOND) && !contains(message, ERR_QUOTA_EXCEEDED)) {
                    log.error("TourAPI 호출 실패: {}, message={}", context, message);
                    throw new CommonException(ErrorCode.EXTERNAL_API_ERROR);
                }
                if (contains(message, ERR_QUOTA_EXCEEDED)) {
                    log.error("TourAPI 일일 요청 한도 초과: {}", context);
                    throw new CommonException(ErrorCode.EXTERNAL_API_QUOTA_EXCEEDED);
                }
                retryOrFail(attempt, context);
                continue;
            }

            if (contains(failureText, ERR_QUOTA_EXCEEDED)) {
                log.error("TourAPI 일일 요청 한도 초과: {}", context);
                throw new CommonException(ErrorCode.EXTERNAL_API_QUOTA_EXCEEDED);
            }
            if (!contains(failureText, ERR_RATE_LIMIT_PER_SECOND)) {
                return body;
            }
            retryOrFail(attempt, context);
        }
    }

    /**
     * 재시도 여유가 남아 있으면 대기하고, 모두 소진했으면 EXTERNAL_API_RATE_LIMITED 로 실패시킨다.
     */
    private void retryOrFail(final int attempt, final String context) {
        if (attempt >= properties.getMaxRetry()) {
            log.error("TourAPI 초당 요청 제한 초과 - 재시도 {}회 모두 실패: {}", properties.getMaxRetry(), context);
            throw new CommonException(ErrorCode.EXTERNAL_API_RATE_LIMITED);
        }
        final long delayMs = (long) properties.getRetryDelayMs() * (attempt + 1);
        log.warn("TourAPI 초당 요청 제한 초과 - {}ms 후 재시도({}/{}): {}",
                delayMs, attempt + 1, properties.getMaxRetry(), context);
        sleepQuietly(delayMs);
    }

    /**
     * TourAPI 초당 요청 제한에 걸리지 않도록 호출 사이에 최소 간격을 둔다.
     */
    private void throttle() {
        final int intervalMs = properties.getRequestIntervalMs();
        if (intervalMs <= 0) {
            return;
        }
        synchronized (throttleLock) {
            final long waitMs = lastRequestAt + intervalMs - System.currentTimeMillis();
            if (waitMs > 0) {
                sleepQuietly(waitMs);
            }
            lastRequestAt = System.currentTimeMillis();
        }
    }

    private void sleepQuietly(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CommonException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    private boolean contains(final String text, final String keyword) {
        return text != null && text.contains(keyword);
    }

    /**
     * 응답 본문에서 items.item 목록을 꺼낸다. 조회 결과가 없으면 빈 목록을 반환한다.
     */
    private List<JsonNode> readItems(final String body, final String context) {
        if (!StringUtils.hasText(body)) {
            log.error("TourAPI 응답이 비어 있습니다: {}", context);
            throw new CommonException(ErrorCode.EXTERNAL_API_ERROR);
        }

        final JsonNode root;
        try {
            root = objectMapper.readTree(body);
        } catch (final Exception e) {
            // 서비스 키 오류 등 게이트웨이 단계에서 막히면 _type=json 이어도 XML 이 내려올 수 있다.
            log.error("TourAPI 응답 파싱 실패: {}, body={}", context, abbreviate(body));
            throw new CommonException(ErrorCode.EXTERNAL_API_ERROR);
        }

        // 인증 실패 등은 정상 응답 구조(response) 대신 OpenAPI_ServiceResponse 로 내려온다.
        if (!root.at("/OpenAPI_ServiceResponse/cmmMsgHeader/errMsg").isMissingNode()) {
            log.error("TourAPI 오류 응답: {}, body={}", context, abbreviate(body));
            throw new CommonException(ErrorCode.EXTERNAL_API_ERROR);
        }

        if (!RESULT_CODE_SUCCESS.equals(root.at("/response/header/resultCode").asText(""))) {
            log.error("TourAPI 실패 코드 응답: {}, body={}", context, abbreviate(body));
            throw new CommonException(ErrorCode.EXTERNAL_API_ERROR);
        }

        // 조회 결과가 0건이면 items 가 객체가 아닌 빈 문자열("")로 내려오므로 배열인지 먼저 확인한다.
        final JsonNode itemNode = root.at("/response/body/items/item");
        if (itemNode.isArray()) {
            final List<JsonNode> nodes = new ArrayList<>(itemNode.size());
            itemNode.forEach(nodes::add);
            return nodes;
        }
        // 1건만 조회될 때 배열이 아닌 단일 객체로 내려오는 경우에도 대응한다.
        if (itemNode.isObject()) {
            return List.of(itemNode);
        }
        return List.of();
    }

    private String abbreviate(final String body) {
        return body.length() <= ERROR_BODY_LOG_LIMIT ? body : body.substring(0, ERROR_BODY_LOG_LIMIT);
    }
}
