package dk.gatchaz.server.common.security;

import dk.gatchaz.server.common.exception.ErrorCode;
import dk.gatchaz.server.member.dto.MemberDetailResponse;
import dk.gatchaz.server.member.mapper.MemberMapper;
import dk.gatchaz.server.common.security.exception.JwtAuthenticationException;
import dk.gatchaz.server.type.ERole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * JwtAuthenticationFilter 가 토큰에서 뽑아낸 memberId 가 실제 존재하고 탈퇴하지 않은 회원인지 검증한다.
 * member.role(ERole: USER/ADMIN) 을 GrantedAuthority("ROLE_USER"/"ROLE_ADMIN") 로 변환해 인증 완료
 * 토큰에 실어주는데, ADMIN 계정은 배치/운영 API 뿐 아니라 일반 USER 전용 API 도 그대로 써야 하므로
 * ROLE_ADMIN 계정에는 ROLE_USER 도 함께 부여한다(역할 계층을 직접 흉내).
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

        final MemberDetailResponse member = memberMapper.selectMember(memberId);
        if (member == null) {
            throw new JwtAuthenticationException(ErrorCode.NOT_FOUND_USER);
        }

        return new JwtAuthenticationToken(memberId, resolveAuthorities(member.getRole()));
    }

    private List<GrantedAuthority> resolveAuthorities(ERole role) {
        if (role == ERole.ADMIN) {
            return List.of(
                    new SimpleGrantedAuthority(ERole.ADMIN.toRoleName()),
                    new SimpleGrantedAuthority(ERole.USER.toRoleName()));
        }
        return List.of(new SimpleGrantedAuthority(ERole.USER.toRoleName()));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
