package dk.gatchaz.server.common.interceptor;

import dk.gatchaz.server.common.dto.ResponseDto;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 컨트롤러가 돌려준 ResponseDto.httpStatus() 값을 실제 HTTP 응답 상태 코드에 반영한다.
 * 이게 없으면 ResponseDto 에 담긴 httpStatus 필드가 응답 바디에만 있고 실제로는 항상 200 으로 내려가서,
 * (지금까지 gatchaz 의 모든 에러 응답이 실제로 이 상태였다) 클라이언트가 HTTP 상태 코드로 성공/실패를
 * 구분할 수 없었다. 이 클래스를 추가하면 앞으로는 CommonException 등으로 만들어진 에러 응답이
 * ErrorCode 에 정의된 실제 상태 코드(400/401/404/409 등)로 내려간다.
 */
@ControllerAdvice
public class ResponseInterceptor implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                   Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                   ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof ResponseDto<?> responseDto) {
            response.setStatusCode(responseDto.httpStatus());
        }
        return body;
    }
}
