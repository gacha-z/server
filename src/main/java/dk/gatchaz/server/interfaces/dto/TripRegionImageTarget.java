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

    /** TourAPI 시군구코드 5자리. 앞 2자리는 시도 코드, 3~5번째 자리는 시군구 코드다. (예: 51110 = 강원(51) 춘천시(110)) */
    private String tripRegionCode;

    private String tripRegionName;
}
