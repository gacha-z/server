package dk.gatchaz.server.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * {@code @Scheduled} 배치 실행을 활성화한다.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
