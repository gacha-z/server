package dk.gatchaz.server.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberCreateResponse {

    /** 생성된 회원 ID */
    private Long memberId;
}
