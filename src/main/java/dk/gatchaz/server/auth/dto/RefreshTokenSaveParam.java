package dk.gatchaz.server.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 리프레시 토큰 발급(INSERT) 용 파라미터. INSERT 후 생성된 refreshTokenId 가 채워진다.
 */
@Getter
@Setter
@Builder
public class RefreshTokenSaveParam {

    /** 생성된 리프레시 토큰 ID (INSERT 후 채워짐) */
    private Long refreshTokenId;

    private Long memberId;
    private String token;
    private LocalDateTime expiresAt;
}
