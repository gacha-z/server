package dk.gatchaz.server.trip.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TripInviteCodeResponse {

    /** 여행 초대 코드 (프론트에서 도메인을 붙여 링크로 사용: travel-gacha.app/trip/{code}) */
    private String inviteCode;
}
