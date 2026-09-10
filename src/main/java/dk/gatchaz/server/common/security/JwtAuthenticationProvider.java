package dk.gatchaz.server.common.security;

import dk.gatchaz.server.common.exception.ErrorCode;
import dk.gatchaz.server.member.mapper.MemberMapper;
import dk.gatchaz.server.common.security.exception.JwtAuthenticationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * JwtAuthenticationFilter 가 토큰에서 뽑아낸 memberId 가 실제 존재하고 탈퇴하지 않은 회원인지 검증한다.
 * gatchaz 는 권한(Role) 개념이 없으므로 GrantedAuthority 는 항상 빈 목록으로 인증 완료 토큰을 만든다.
 * (참고 코드는 supports() 에서 UsernamePasswordAuthenticationToken 도 허용한다고 해놓고 authenticate() 는
 * JwtAuthenticationToken 만 처리해서, 실제로 UsernamePasswordAuthenticationToken 이 들어오면
 * userPrincipal 이 null 인 채로 NPE 가 나는 버그가 있었다. 여기서는 supports() 를
 * JwtAuthenticationToken 하나로만 한정해서 그 불일치를 없앴다.)
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final MemberMapper memberMapper;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        final Long memberId = (Long) authentication.getPrincipal();

        if (memberMapper.selectMember(memberId) == null) {
            throw new JwtAuthenticationException(ErrorCode.NOT_FOUND_USER);
        }

        return new JwtAuthenticationToken(memberId, Collections.emptyList());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
