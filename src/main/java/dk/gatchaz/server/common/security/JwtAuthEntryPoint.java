package dk.gatchaz.server.common.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * 인증되지 않은 요청(토큰 없음/만료/위조 등)이 인증이 필요한 API 에 들어왔을 때 Spring Security 가 호출한다.
 * 직접 응답 본문을 만들지 않고 Spring MVC 의 HandlerExceptionResolver 로 예외를 다시 넘겨서,
 * GlobalExceptionHandler(@RestControllerAdvice) 가 다른 예외들과 동일한 형식(ResponseDto)으로 응답하게 한다.
 */
@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    private final HandlerExceptionResolver resolver;

    public JwtAuthEntryPoint(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        resolver.resolveException(request, response, null, authException);
    }
}
