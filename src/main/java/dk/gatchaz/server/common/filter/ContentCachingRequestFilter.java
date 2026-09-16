package dk.gatchaz.server.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

/**
 * 요청 바디를 캐싱해서, HttpMessageNotReadableException(JSON 파싱 실패) 발생 시
 * GlobalExceptionHandler 가 실제로 어떤 값이 들어왔는지 로그로 남길 수 있게 한다.
 * (Jackson 이 한 번 읽어버린 입력 스트림은 다시 읽을 수 없어서, 캐싱 없이는 예외 핸들러
 * 시점에 원본 바디를 확인할 방법이 없다) 응답 바디는 건드리지 않는다.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ContentCachingRequestFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
                                     final FilterChain filterChain) throws ServletException, IOException {
        filterChain.doFilter(new ContentCachingRequestWrapper(request), response);
    }

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) {
        // multipart(셋로그 업로드 등)는 대용량 바이너리라 캐싱하면 메모리 낭비가 크고,
        // 어차피 JSON 파싱 대상이 아니라 캐싱할 필요가 없다.
        final String contentType = request.getContentType();
        return contentType != null && contentType.startsWith("multipart/");
    }
}
