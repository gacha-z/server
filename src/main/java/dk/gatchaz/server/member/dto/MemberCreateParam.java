package dk.gatchaz.server.member.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 회원가입(INSERT) 용 파라미터. INSERT 후 생성된 memberId 가 채워진다.
 */
@Getter
@Setter
@Builder
public class MemberCreateParam {

    /** 생성된 회원 ID (INSERT 후 채워짐) */
    private Long memberId;

    private String nickname;
    private Integer age;
}
