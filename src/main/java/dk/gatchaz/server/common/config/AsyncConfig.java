package dk.gatchaz.server.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * {@code @Async} 비동기 실행을 활성화한다.
 * 알림 발송처럼 응답을 늦추면 안 되는 작업을 별도 스레드에서 처리하는 데 쓴다.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
