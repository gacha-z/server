package dk.gatchaz.server.common.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * HTTP 요청 헤더에서 접두사(prefix)를 제거한 값을 꺼내는 유틸리티. (예: "Authorization: Bearer xxx" → "xxx")
 */
public class HeaderUtil {

    private HeaderUtil() {
    }

    public static Optional<String> refineHeader(HttpServletRequest request, String header, String prefix) {
        final String rawValue = request.getHeader(header);

        if (!StringUtils.hasText(rawValue) || !rawValue.startsWith(prefix)) {
            return Optional.empty();
        }

        return Optional.of(rawValue.substring(prefix.length()));
    }
}
