package dk.gatchaz.server.common.exception;

import dk.gatchaz.server.common.dto.ResponseDto;
import dk.gatchaz.server.common.security.exception.JwtAuthenticationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseDto<?> handleAccessDeniedException(AccessDeniedException e) {
        log.error("Handler in AccessDeniedException Error Message = " + e.getMessage());
        return ResponseDto.fail(new CommonException(ErrorCode.ACCESS_DENIED_ERROR));
    }

    // JwtAuthenticationFilter/JwtAuthenticationProvider 가 던진 JwtAuthenticationException 이면
    // 거기 담긴 구체적인 ErrorCode(만료/위조/타입불일치 등)를 그대로 쓰고, 그 외 일반적인
    // AuthenticationException 이면 INVALID_TOKEN_ERROR 로 처리한다.
    // (참고 코드는 항상 INVALID_TOKEN_ERROR 고정이었는데, JwtAuthEntryPoint 를 거쳐 여기로 넘어오는
    // 예외를 JwtAuthenticationException 으로 만들면서 구체적인 사유를 그대로 응답에 실을 수 있게 됐다.)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseDto<?> handleAuthenticationException(AuthenticationException e) {
        log.error("Handler in AuthenticationException Error Message = " + e.getMessage());
        final ErrorCode errorCode = (e instanceof JwtAuthenticationException jwtAuthenticationException)
                ? jwtAuthenticationException.getErrorCode()
                : ErrorCode.INVALID_TOKEN_ERROR;
        return ResponseDto.fail(new CommonException(errorCode));
    }

    @ExceptionHandler({HttpMediaTypeNotSupportedException.class, MultipartException.class})
    public ResponseDto<?> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        log.error("Handler in HttpMediaTypeNotSupportedException Error Message = " + e.getMessage());
        return ResponseDto.fail(new CommonException(ErrorCode.UNSUPPORTED_MEDIA_TYPE));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseDto<?> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.error("Handler in NoHandlerFoundException Error Message = " + e.getMessage());
        return ResponseDto.fail(new CommonException(ErrorCode.NOT_END_POINT));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseDto<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException e,
                                                                  HttpServletRequest request) {
        log.error("Handler in HttpMessageNotReadableException Error Message = {}, Request Body = {}",
                e.getMessage(), requestBody(request));
        return ResponseDto.fail(new CommonException(ErrorCode.INVALID_ARGUMENT));
    }

    /**
     * ContentCachingRequestFilter 가 캐싱해둔 원본 요청 바디를 꺼낸다. (Jackson 이 이미 소비한
     * 입력 스트림은 다시 못 읽으므로, 캐싱된 바이트 배열에서만 확인 가능하다) 캐싱 대상이 아니었거나
     * (multipart 등) 바디가 없으면 그 사실을 그대로 로그에 남긴다.
     */
    private static final int REQUEST_BODY_LOG_LIMIT = 2000;

    private String requestBody(final HttpServletRequest request) {
        if (!(request instanceof ContentCachingRequestWrapper wrapper)) {
            return "(캐싱되지 않음)";
        }
        final byte[] content = wrapper.getContentAsByteArray();
        if (content.length == 0) {
            return "(빈 바디)";
        }
        final String body = new String(content, StandardCharsets.UTF_8);
        return body.length() > REQUEST_BODY_LOG_LIMIT
                ? body.substring(0, REQUEST_BODY_LOG_LIMIT) + "...(생략)"
                : body;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseDto<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("Handler in MethodArgumentNotValidException Error Message = " + e.getMessage());
        return ResponseDto.fail(new CommonException(ErrorCode.INVALID_ARGUMENT));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseDto<?> handleHandlerMethodValidationException(HandlerMethodValidationException e) {
        log.error("Handler in HandlerMethodValidationException Error Message = " + e.getMessage());
        return ResponseDto.fail(e);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseDto<?> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("Handler in HttpRequestMethodNotSupportedException Error Message = " + e.getMessage());
        return ResponseDto.fail(new CommonException(ErrorCode.METHOD_NOT_ALLOWED));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseDto<?> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error("Handler in MethodArgumentTypeMismatchException Error Message = " + e.getMessage());
        return ResponseDto.fail(new CommonException(ErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseDto<?> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.error("Handler in MissingServletRequestParameterException Error Message = " + e.getMessage());
        return ResponseDto.fail(new CommonException(ErrorCode.MISSING_REQUEST_PARAMETER));
    }

    @ExceptionHandler(CommonException.class)
    public ResponseDto<?> handleApiException(CommonException e) {
        log.error("Handler in CommonException Error Message = " + e.getMessage());
        return ResponseDto.fail(e);
    }

    @ExceptionHandler(Exception.class)
    public ResponseDto<?> handleException(Exception e) {
        log.error("Handler in Exception Error Message = " + e.getMessage(), e);
        return ResponseDto.fail(new CommonException(ErrorCode.SERVER_ERROR));
    }

}
