package dk.gatchaz.server.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 소셜 로그인(구글/애플 공통) 결과.
 */
@Getter
@Builder
@Schema(description = "소셜 로그인 결과")
public class SocialLoginResponse {

    @Schema(description = "회원 ID", example = "1")
    private Long memberId;

    @Schema(description = "액세스 토큰")
    private String accessToken;

    @Schema(description = "리프레시 토큰")
    private String refreshToken;

    @Schema(description = "이번 로그인으로 회원이 새로 생성됐는지 여부. true면 온보딩(닉네임/나이 입력)이 필요하다.", example = "true")
    private Boolean newMember;
}
