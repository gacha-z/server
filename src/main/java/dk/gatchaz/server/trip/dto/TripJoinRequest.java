package dk.gatchaz.server.trip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    /** 참여할 회원 ID (로그인 연동 전까지 요청으로 받는다) */
    @NotNull
    private Long memberId;
}
