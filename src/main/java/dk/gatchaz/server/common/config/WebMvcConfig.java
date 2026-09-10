package dk.gatchaz.server.common.config;

import dk.gatchaz.server.common.interceptor.UserIdResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @UserId 인자 리졸버를 등록한다.
 * (참고 코드는 여기에 @EnableWebMvc 도 붙어 있었는데, Spring Boot 프로젝트에서 @EnableWebMvc 를 붙이면
 * Boot 가 자동으로 설정해주는 MVC 구성(Jackson 컨버터, 정적 리소스 처리 등)이 전부 꺼지고 직접 다 구성해야
 * 하는 부작용이 있다. gatchaz 는 그 자동 구성에 의존하고 있어서 빼는 게 맞다고 판단했다.)
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final UserIdResolver userIdResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(userIdResolver);
    }
}
