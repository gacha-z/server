package dk.gatchaz.server.common.config;

import dk.gatchaz.server.common.security.JwtAuthEntryPoint;
import dk.gatchaz.server.common.security.JwtAuthenticationProvider;
import dk.gatchaz.server.common.security.filter.JwtAuthenticationFilter;
import dk.gatchaz.server.common.security.handler.JwtAccessDeniedHandler;
import dk.gatchaz.server.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

/**
 * 앱(모바일) 전용 로그인 - Spring 의 oauth2Login(브라우저 리다이렉트 인가 코드 교환)은 쓰지 않는다.
 * Google/Apple SDK 로 앱이 직접 발급받은 id_token/identityToken 을 AuthController 가 검증하고
 * 우리 자체 JWT(액세스/리프레시)를 발급해주는 구조라, 여기서는 그 JWT 를 검사하는 필터체인만 구성하면 된다.
 * 그래서 참고 코드에 있던 oauth2Login(...), CustomOAuth2UserService, 각종 OAuth2LoginSuccess/FailureHandler,
 * 쿠키 기반 로그아웃(CustomLogOutProcessHandler/ResultHandler) 등은 전부 포팅하지 않았다.
 * 로그아웃도 Spring Security 의 LogoutFilter 가 아니라 AuthController 의 일반 REST 엔드포인트
 * (POST /api/v1/auth/logout)로 처리하므로 logout() DSL 은 꺼둔다.
 *
 * 아직 로그인 연동 전인 기존 도메인 API(Trip/Mission/Diary 등)가 대부분이다(파라미터로 memberId 를
 * 직접 받는 임시 방식 - 각 Service 에 남아있는 "로그인 연동 후 인증된 사용자로 교체" TODO 주석 참고).
 * 지금 당장 anyRequest().authenticated() 로 걸면 그 API 들이 전부 깨지기 때문에, 이번 작업에서는
 * 로그아웃(POST /api/v1/auth/logout) 하나만 인증을 요구하고 나머지는 지금처럼 permitAll() 로 유지했다.
 * 기존 컨트롤러들을 @UserId 기반 인증 사용자로 옮기는 건 이후 별도 작업으로 남겨둔다.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final JwtAuthenticationProvider jwtAuthenticationProvider;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(registry -> registry
                        .requestMatchers("/api/v1/auth/logout").authenticated()
                        .anyRequest().permitAll())
                .exceptionHandling(configurer -> configurer
                        .authenticationEntryPoint(jwtAuthEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler))
                // ExceptionTranslationFilter 바로 앞(=AuthorizationFilter 바로 앞)에 꽂아야
                // 이 필터가 던지는 JwtAuthenticationException 을 ExceptionTranslationFilter 가 잡아서
                // jwtAuthEntryPoint 로 넘겨준다. UsernamePasswordAuthenticationFilter 앞에 꽂으면
                // (참고 코드가 그렇게 하고 있었음) ExceptionTranslationFilter 보다 훨씬 앞이라
                // 예외가 안 잡히고 그대로 500 으로 터진다.
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtUtil, jwtAuthenticationProvider),
                        AuthorizationFilter.class)
                .build();
    }
}
