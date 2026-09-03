package dk.gatchaz.server.interfaces.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 한국관광공사 국문 관광정보 서비스(TourAPI 4.0) 연동 설정.
 * 값은 Server-Secret 의 프로파일별 yml(tour-api)에서 관리한다.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "tour-api")
public class TourApiProperties {

    /** API 기본 주소 (KorService2) */
    private String baseUrl;

    /** 공공데이터포털에서 발급받은 서비스 키 (Decoding 값). URL 조립 시 인코딩해서 사용한다. */
    private String serviceKey;

    /** 필수 요청 파라미터 MobileOS */
    private String mobileOs;

    /** 필수 요청 파라미터 MobileApp */
    private String mobileApp;

    // 아래 값들은 튜닝용이므로 yml 에 없어도 애플리케이션이 기동되도록 기본값을 둔다. (yml 값이 있으면 그 값으로 덮어쓴다)

    /** 조회할 관광 타입 (12: 관광지). 축제·음식점 이미지가 지역 대표로 잡히는 것을 막는다. */
    private int contentTypeId = 12;

    /** 지역당 조회 건수. 첫 건의 대표 이미지가 비어 있을 때 다음 건을 확인하기 위한 여유분이다. */
    private int numOfRows = 10;

    /** 연결 타임아웃 (ms) */
    private int connectTimeoutMs = 3000;

    /** 응답 대기 타임아웃 (ms) */
    private int readTimeoutMs = 5000;

    /** 호출 간 최소 간격 (ms). TourAPI 초당 요청 제한에 걸리지 않도록 요청 속도를 낮춘다. */
    private int requestIntervalMs = 200;

    /** 초당 요청 제한에 걸렸을 때 재시도 횟수 */
    private int maxRetry = 2;

    /** 재시도 대기 시간 (ms). 시도마다 이 값의 배수로 늘어난다. */
    private int retryDelayMs = 1000;
}
