package dk.gatchaz.server.common.security.exception;

import dk.gatchaz.server.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

/**
 * JWT 인증 과정(필터/Provider)에서 발생한 실패를 구체적인 ErrorCode 와 함께 전달하기 위한 예외.
 * Spring Security 의 ExceptionTranslationFilter 가 이 예외를 잡아 AuthenticationEntryPoint(JwtAuthEntryPoint) 로
 * 넘기고, JwtAuthEntryPoint 는 다시 HandlerExceptionResolver 를 통해 GlobalExceptionHandler 로 넘긴다.
 * (참고 코드는 request.setAttribute("exception", ...) 로 값만 심어두고 아무도 읽지 않는 죽은 코드였는데,
 * 이 예외 타입으로 대체해서 값이 실제로 GlobalExceptionHandler 까지 전달되도록 고쳤다.)
 */
@Getter
public class JwtAuthenticationException extends AuthenticationException {

    private final ErrorCode errorCode;

    public JwtAuthenticationException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
