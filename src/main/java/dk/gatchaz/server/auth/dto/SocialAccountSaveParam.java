package dk.gatchaz.server.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 소셜 계정 연결(INSERT) 용 파라미터. INSERT 후 생성된 socialAccountId 가 채워진다.
 */
@Getter
@Setter
@Builder
public class SocialAccountSaveParam {

    /** 생성된 소셜 계정 ID (INSERT 후 채워짐) */
    private Long socialAccountId;

    private Long memberId;
    private String provider;
    private String providerUserId;
    private String email;
}
