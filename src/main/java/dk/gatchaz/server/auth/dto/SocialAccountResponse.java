package dk.gatchaz.server.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 소셜 계정 조회 결과. (로그인 시 기존 가입 여부 판단용)
 */
@Getter
@Setter
@NoArgsConstructor
public class SocialAccountResponse {

    private Long socialAccountId;
    private Long memberId;
    private String provider;
    private String providerUserId;
    private String email;
    private LocalDateTime createdAt;
}
