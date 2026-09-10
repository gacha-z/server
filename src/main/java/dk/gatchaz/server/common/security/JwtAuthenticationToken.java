package dk.gatchaz.server.common.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

/**
 * JWT 로 인증된(혹은 인증 시도 중인) 사용자를 나타내는 Authentication 구현체.
 * gatchaz 는 회원에 별도 권한(Role) 개념이 없으므로, principal 은 memberId(Long) 하나만 들고 다닌다.
 */
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final Long memberId;

    /** 인증 시도 전(필터에서 토큰만 파싱한 상태)의 미인증 토큰. */
    public JwtAuthenticationToken(Long memberId) {
        super(Collections.emptyList());
        this.memberId = memberId;
        setAuthenticated(false);
    }

    /** JwtAuthenticationProvider 검증을 통과한 인증 완료 토큰. */
    public JwtAuthenticationToken(Long memberId, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.memberId = memberId;
        super.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return memberId;
    }
}
