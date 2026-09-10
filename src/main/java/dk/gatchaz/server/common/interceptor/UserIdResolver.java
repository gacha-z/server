package dk.gatchaz.server.common.interceptor;

import dk.gatchaz.server.common.annotation.UserId;
import dk.gatchaz.server.common.exception.CommonException;
import dk.gatchaz.server.common.exception.ErrorCode;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 컨트롤러 파라미터에 붙은 @UserId 를 SecurityContext 의 인증 정보(JwtAuthenticationToken.principal = memberId)로 채워준다.
 *
 * 참고 코드는 별도의 UserIdInterceptor 가 SecurityContext 의 Authentication 을 "USER_ID" 라는 request
 * attribute(문자열)로 옮겨 담고, 이 리졸버는 그 attribute 만 읽는 2단계 구조였다. 그런데 인터셉터 쪽에
 * Authentication 이 null 인지 확인하는 코드가 없어서, 로그인이 필요 없는 요청(SecurityContext 가 비어있음)에서
 * NPE 가 나는 버그가 있었다. 여기서는 인터셉터를 없애고 이 리졸버가 SecurityContext 를 직접 읽으면서
 * null/미인증/익명 사용자를 전부 방어하도록 합쳐서 그 버그를 없앴다.
 */
@Component
public class UserIdResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(Long.class)
                && parameter.hasParameterAnnotation(UserId.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                   NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || !(authentication.getPrincipal() instanceof Long memberId)) {
            throw new CommonException(ErrorCode.ACCESS_DENIED_ERROR);
        }

        return memberId;
    }
}
