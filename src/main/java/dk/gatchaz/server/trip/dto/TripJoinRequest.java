package dk.gatchaz.server.trip.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TripJoinRequest {

    /** 초대 링크의 코드 (travel-gacha.app/trip/{code} 의 code 부분) */
    @NotBlank
    private String code;
}
