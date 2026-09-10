package dk.gatchaz.server.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 유효한(폐기되지 않고 만료되지 않은) 리프레시 토큰 조회 결과. (재발급/검증용)
 */
@Getter
@Setter
@NoArgsConstructor
public class RefreshTokenResponse {

    private Long refreshTokenId;
    private Long memberId;
    private String token;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
