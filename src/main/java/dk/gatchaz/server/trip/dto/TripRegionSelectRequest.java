package dk.gatchaz.server.trip.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TripRegionSelectRequest {

    /** 지역을 선택할 여행 ID */
    @NotNull
    private Long tripId;

    /** 사용자가 최종 선택한 여행 지역 ID (trip_candidate 중 selected_yn='Y'로 확정할 지역) */
    @NotNull
    private Long tripRegionId;
}
