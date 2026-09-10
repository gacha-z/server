package dk.gatchaz.server.common.security.filter;

import dk.gatchaz.server.common.constant.Constant;
import dk.gatchaz.server.common.exception.ErrorCode;
import dk.gatchaz.server.common.security.JwtAuthenticationProvider;
import dk.gatchaz.server.common.security.JwtAuthenticationToken;
import dk.gatchaz.server.common.security.exception.JwtAuthenticationException;
import dk.gatchaz.server.common.util.HeaderUtil;
import dk.gatchaz.server.common.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Authorization 헤더에 Bearer 액세스 토큰이 있으면 검증해서 SecurityContext 에 인증 정보를 채운다.
 * 토큰이 아예 없으면 그냥 익명으로 통과시킨다 - 이 요청이 로그인을 요구하는지는 여기서 판단하지 않고
 * SecurityConfig 의 authorizeHttpRequests 설정(permitAll / authenticated)에 맡긴다.
 * gatchaz 는 아직 로그인 연동 전 컨트롤러가 대부분이라(파라미터로 memberId 를 직접 받는 임시 방식),
 * 토큰이 없다고 무조건 막아버리면 기존에 동작하던 API 가 전부 깨지기 때문에 이렇게 설계했다.
 * 다만 토큰이 있는데 위조/만료 등으로 유효하지 않으면 그 자리에서 바로 막는다(무시하고 넘기지 않음).
 *
 * (참고 코드 대비 고친 점)
 * - 참고 코드는 토큰이 없으면 무조건 실패 처리했다(모든 API 가 로그인을 요구하는 구조였기 때문).
 *   gatchaz 는 아직 그렇지 않아서 위와 같이 "토큰 있으면 검증, 없으면 익명 통과"로 바꿨다.
 * - 매 요청마다 Authorization 헤더 원문을 그대로 log.info 로 남겼는데, 토큰이 로그에 그대로
 *   노출되는 문제라 그 로깅은 걷어냈다.
 * - 예외를 request.setAttribute 로 심어두고 아무도 안 읽는 죽은 코드(JwtExceptionFilter) 대신,
 *   JwtAuthenticationException 을 던져서 Spring Security 의 표준 예외 처리 경로를 그대로 타게 했다.
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final JwtAuthenticationProvider jwtAuthenticationProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String token = HeaderUtil.refineHeader(request, Constant.AUTHORIZATION_HEADER, Constant.BEARER_PREFIX)
                .orElse(null);

        if (token != null) {
            authenticate(token, request);
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(String token, HttpServletRequest request) {
        final Long memberId;
        try {
            final Claims claims = jwtUtil.validateToken(token);
            memberId = Long.valueOf(claims.get(Constant.USER_ID_CLAIM_NAME, String.class));
        } catch (ExpiredJwtException e) {
            throw new JwtAuthenticationException(ErrorCode.EXPIRED_TOKEN_ERROR);
        } catch (MalformedJwtException e) {
            throw new JwtAuthenticationException(ErrorCode.TOKEN_MALFORMED_ERROR);
        } catch (UnsupportedJwtException e) {
            throw new JwtAuthenticationException(ErrorCode.TOKEN_UNSUPPORTED_ERROR);
        } catch (IllegalArgumentException e) {
            throw new JwtAuthenticationException(ErrorCode.TOKEN_TYPE_ERROR);
        } catch (JwtException e) {
            log.warn("예상치 못한 JwtException 으로 토큰 검증 실패: {}", e.getMessage());
            throw new JwtAuthenticationException(ErrorCode.TOKEN_UNKNOWN_ERROR);
        }

        final JwtAuthenticationToken beforeAuthentication = new JwtAuthenticationToken(memberId);
        final JwtAuthenticationToken afterAuthentication =
                (JwtAuthenticationToken) jwtAuthenticationProvider.authenticate(beforeAuthentication);
        afterAuthentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        final SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(afterAuthentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 로그인/리프레시 요청은 애초에 토큰을 검증할 필요가 없으니, 옛 클라이언트가 헤더에
        // 유효하지 않은 값을 실수로 붙여 보내더라도 실패하지 않도록 아예 건너뛴다.
        final String uri = request.getRequestURI();
        return Constant.NO_NEED_AUTH_URLS.contains(uri)
                || uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/webjars");
    }
}
