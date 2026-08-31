package dk.gatchaz.server.mission.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 위치 인증에 필요한 여행 지역의 중심 좌표.
 */
@Getter
@Setter
@NoArgsConstructor
public class TripRegionCoordinate {
    private BigDecimal latitude;
    private BigDecimal longitude;
}
