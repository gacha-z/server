package dk.gatchaz.server.common.security.swagger;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Swagger UI/API 문서 열람 전용 계정(swagger_account 테이블) 인증.
 * 앱 회원 시스템(JWT)과는 완전히 별개이며, SecurityConfig 의 swagger 전용 필터체인에서만 쓰인다.
 */
@Service
@RequiredArgsConstructor
public class SwaggerUserDetailsService implements UserDetailsService {

    private final SwaggerAccountMapper swaggerAccountMapper;

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        final SwaggerAccountInfo account = swaggerAccountMapper.selectByUsername(username);
        if (account == null) {
            throw new UsernameNotFoundException("존재하지 않는 스웨거 계정: " + username);
        }
        return new User(account.getUsername(), account.getPassword(), List.of());
    }
}
