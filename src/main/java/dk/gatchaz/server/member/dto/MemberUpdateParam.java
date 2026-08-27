package dk.gatchaz.server.member.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 회원 정보 부분 수정(UPDATE) 용 파라미터. null 인 필드는 수정하지 않고 기존 값을 유지한다.
 */
@Getter
@Builder
public class MemberUpdateParam {

    private Long memberId;
    private String nickname;
    private Integer age;
}
