package dk.gatchaz.server.common.config;

import dk.gatchaz.server.common.security.JwtAuthEntryPoint;
import dk.gatchaz.server.common.security.JwtAuthenticationProvider;
import dk.gatchaz.server.common.security.filter.JwtAuthenticationFilter;
import dk.gatchaz.server.common.security.handler.JwtAccessDeniedHandler;
import dk.gatchaz.server.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
 * 각 도메인 컨트롤러의 "현재 로그인한 사용자"를 나타내던 memberId 파라미터/DTO 필드를 전부 @UserId 로
 * 옮긴 뒤, 엔드포인트 하나하나가 아니라 도메인(경로 prefix) 단위로 묶어서 권한(role)을 걸었다.
 * 예를 들어 여행 상세 조회(GET /api/v1/trips/{id})처럼 원래 memberId 를 안 받던 조회 API도
 * "/api/v1/trips/**" 에 걸려 함께 인증이 필요해지는데, 앱 전체가 로그인 후 사용을 전제로 하므로
 * 지금 단계에서는 이 정도로 단순화한다. 나중에 도메인 안에서 특정 API 만 공개로 열어야 하면
 * 그 경로를 이 목록보다 먼저(더 위에) permitAll() 로 추가하면 된다 - authorizeHttpRequests 는
 * 먼저 매치되는 규칙을 쓰기 때문이다. (회원가입 POST /api/v1/members 를 permitAll 로 먼저 둔 것이 그 예)
 *
 * mission(/api/v1/trips/{tripId}/missions/**)과 setlog 목록 조회(/api/v1/trips/{tripId}/setlogs)는
 * 경로가 "/api/v1/trips/**" 하위라 trip 규칙에 자연히 포함된다.
 *
 * member.role(ERole: USER/ADMIN) 기반 권한 분리:
 * - 배치/운영 API(/api/v1/batch/**, 예: 지역 이미지 적재·일기 알림 발송 수동 실행)는 hasRole("ADMIN") 으로 잠근다.
 *   앱 사용자가 호출할 API 가 아니라 운영자만 써야 하기 때문이다.
 * - 그 외 로그인이 필요한 도메인 API 는 hasRole("USER") 로 건다.
 * - ADMIN 계정도 일반 앱 기능(여행/일기/알림 등)을 그대로 써야 하므로, JwtAuthenticationProvider 가
 *   ADMIN 회원에게 ROLE_ADMIN 과 ROLE_USER 를 함께 부여한다 - 여기서 hasRole("USER") 는 ADMIN 도 통과한다.
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
                        // batch - 운영자 전용 수동 실행 API, ADMIN 만 허용
                        .requestMatchers("/api/v1/batch/**").hasRole("ADMIN")

                        // auth - 로그인/재발급은 공개, 로그아웃만 인증 필요
                        .requestMatchers("/api/v1/auth/logout").hasRole("USER")

                        // member - 회원가입(POST)만 공개, 나머지(/me 조회·수정·탈퇴)는 인증 필요
                        .requestMatchers(HttpMethod.POST, "/api/v1/members").permitAll()
                        .requestMatchers("/api/v1/members/**").hasRole("USER")

                        // trip - mission(trips/{id}/missions/**), setlog 목록 조회(trips/{id}/setlogs)도 이 하위 경로라 함께 인증 필요
                        .requestMatchers("/api/v1/trips/**").hasRole("USER")

                        // diary
                        .requestMatchers("/api/v1/diaries/**").hasRole("USER")

                        // notification
                        .requestMatchers("/api/v1/devices/**").hasRole("USER")
                        .requestMatchers("/api/v1/notifications/**").hasRole("USER")

                        // setlog (업로드/다운로드 - 목록 조회는 위 trip 규칙에 포함)
                        .requestMatchers("/api/v1/setlogs/**").hasRole("USER")
                        .requestMatchers("/api/v1/missions/**").hasRole("USER")

                        // collection
                        .requestMatchers("/api/v1/collection/**").hasRole("USER")

                        // 그 외(swagger, api-docs 등) 공개
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
