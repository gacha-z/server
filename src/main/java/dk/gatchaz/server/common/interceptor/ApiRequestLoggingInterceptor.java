package dk.gatchaz.server.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 어떤 요청이 들어왔는지 서버 로그에 남긴다.
 * - 실제 컨트롤러 메서드(HandlerMethod)로 매핑된 요청은 정상 API 호출로 INFO 레벨에 남긴다.
 * - 그 외(정적 리소스 핸들러로 떨어지는 요청 포함)는 존재하지 않는 API/스캐너 트래픽일 수 있어
 *   WARN 레벨로 구분해서 남긴다. (예: ".env", "wp-login.php" 같은 경로 스캔 요청)
 *   Spring Boot 는 "/**" 에 정적 리소스 핸들러를 기본 등록해두므로, 이런 요청도 대부분 handler 를
 *   찾아 preHandle 까지는 도달하고 실제 처리 중에 "No static resource ..." 예외로 이어진다.
 */
@Slf4j
public class ApiRequestLoggingInterceptor implements HandlerInterceptor {

    private static final String START_TIME_ATTRIBUTE = "apiRequestStartTimeMs";

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response,
                              final Object handler) {
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        if (handler instanceof HandlerMethod) {
            log.info("API 호출: {} {}", request.getMethod(), requestUriWithQuery(request));
        } else {
            log.warn("매핑되지 않은 요청: {} {} from {} (handler={})",
                    request.getMethod(), requestUriWithQuery(request), clientIp(request),
                    handler.getClass().getSimpleName());
        }
        return true;
    }

    @Override
    public void afterCompletion(final HttpServletRequest request, final HttpServletResponse response,
                                 final Object handler, final Exception ex) {
        final Object startedAt = request.getAttribute(START_TIME_ATTRIBUTE);
        final long elapsedMs = startedAt instanceof Long ? System.currentTimeMillis() - (Long) startedAt : -1;
        if (handler instanceof HandlerMethod) {
            log.info("API 응답: {} {} -> {} ({}ms)",
                    request.getMethod(), requestUriWithQuery(request), response.getStatus(), elapsedMs);
        } else {
            log.warn("매핑되지 않은 요청 응답: {} {} -> {} ({}ms)",
                    request.getMethod(), requestUriWithQuery(request), response.getStatus(), elapsedMs);
        }
    }

    private String requestUriWithQuery(final HttpServletRequest request) {
        final String query = request.getQueryString();
        return StringUtils.hasText(query) ? request.getRequestURI() + "?" + query : request.getRequestURI();
    }

    /**
     * nginx 뒤에서 동작하므로(X-Forwarded-For/X-Real-IP 설정됨) 그 값을 우선 쓰고, 없으면
     * getRemoteAddr() 로 대체한다. X-Forwarded-For 는 "클라이언트, 프록시1, 프록시2 ..." 순이라 첫 값만 쓴다.
     */
    private String clientIp(final HttpServletRequest request) {
        final String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }
        final String realIp = request.getHeader("X-Real-IP");
        return StringUtils.hasText(realIp) ? realIp : request.getRemoteAddr();
    }
}
