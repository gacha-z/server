package dk.gatchaz.server.trip.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TripRerollRequest {

    @NotNull
    private Long tripId;

    @NotNull
    private Long tripCandidateId;
}
