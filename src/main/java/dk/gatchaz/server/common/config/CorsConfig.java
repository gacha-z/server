package dk.gatchaz.server.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {
    
    // Spring Security의 SecurityConfig에서 이 Bean을 그대로 CORS 설정으로 사용한다.
    // 앱(모바일) 전용 로그인이라 인증은 쿠키가 아닌 Authorization 헤더의 Bearer 토큰으로만 이뤄지므로
    // 쿠키 전송(allowCredentials)이 필요 없다. allowedOrigins("*") + allowCredentials(true) 조합은
    // 스펙상 허용되지 않아(Spring 5.3+에서 예외 발생) 그대로 두면 서버가 기동조차 되지 않는 버그였음.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(false);
        config.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
