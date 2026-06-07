package dk.gatchaz.server.exception;

import dk.gatchaz.server.dto.ResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

//    @ExceptionHandler(AccessDeniedException.class)
//    public ResponseDto<?> handleAccessDeniedException(AccessDeniedException e) {
//        log.error("Handler in AccessDeniedException Error Message = " + e.getMessage());
//        return ResponseDto.fail(new CommonException(ErrorCode.ACCESS_DENIED_ERROR));
//    }
//
//    @ExceptionHandler(AuthenticationException.class)
//    public ResponseDto<?> handleAuthenticationException(AuthenticationException e) {
//        log.error("Handler in AuthenticationException Error Message = " + e.getMessage());
//        return ResponseDto.fail(new CommonException(ErrorCode.INVALID_TOKEN_ERROR));
//    }
//
//    @ExceptionHandler(OAuth2AuthenticationException.class)
//    public ResponseDto<?> handleOAuth2AuthenticationException(OAuth2AuthenticationException e) {
//        log.error("Handler in OAuth2AuthenticationException Error Message = " + e.getMessage());
//        return ResponseDto.fail(new CommonException(ErrorCode.SOCIAL_LOGIN_ERROR));
//    }

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
    public ResponseDto<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("Handler in HttpMessageNotReadableException Error Message = " + e.getMessage());
        return ResponseDto.fail(new CommonException(ErrorCode.INVALID_ARGUMENT));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseDto<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("Handler in MethodArgumentNotValidException Error Message = " + e.getMessage());
        return ResponseDto.fail(new CommonException(ErrorCode.INVALID_ARGUMENT));
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
