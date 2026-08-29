package dk.gatchaz.server.interfaces.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 지역 대표 이미지 적재 대상 지역. (trip_region 에서 조회)
 */
@Getter
@Setter
@NoArgsConstructor
public class TripRegionImageTarget {

    private Long tripRegionId;

    /** 법정동 코드 10자리. 앞 2자리는 시도 코드, 3~5번째 자리는 시군구 코드다. (예: 5111000000 = 강원(51) 춘천시(110)) */
    private String tripRegionCode;

    private String tripRegionName;
}
