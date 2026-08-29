package dk.gatchaz.server.interfaces.support;

import dk.gatchaz.server.interfaces.dto.LdongCode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TourAPI 법정동 코드 목록(ldongCode2)을 배치 1회 실행 동안 재사용하기 위한 캐시.
 * 지역마다 같은 목록을 다시 조회하면 호출 수가 지역 수만큼 늘어나므로, 처음 필요한 시점에 한 번만 조회한다.
 *
 * 실행할 때마다 새로 만들어 쓰는 일회성 객체다. (스프링 빈이 아니므로 실행 간에 값이 남지 않는다)
 */
public class LdongCodeCache {

    private final TourApiClient tourApiClient;

    /** 시도 목록 */
    private List<LdongCode> regnCodes;

    /** 시도 코드 → 그 시도의 시군구 목록 */
    private final Map<String, List<LdongCode>> signguCodes = new HashMap<>();

    public LdongCodeCache(final TourApiClient tourApiClient) {
        this.tourApiClient = tourApiClient;
    }

    /**
     * 시도 목록을 반환한다. (예: 41 경기도, 36110 세종특별자치시)
     */
    public List<LdongCode> regnCodes() {
        if (regnCodes == null) {
            regnCodes = tourApiClient.findLdongCodes(null);
        }
        return regnCodes;
    }

    /**
     * 해당 시도(lDongRegnCd)의 시군구 목록을 반환한다. (예: 110 수원시, 111 수원시 장안구)
     */
    public List<LdongCode> signguCodes(final String lDongRegnCd) {
        return signguCodes.computeIfAbsent(lDongRegnCd, tourApiClient::findLdongCodes);
    }
}
