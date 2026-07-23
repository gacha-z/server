package dk.gatchaz.server.trip.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TripJoinResponse {

    /** 참여한 여행 ID */
    private Long tripId;
}
