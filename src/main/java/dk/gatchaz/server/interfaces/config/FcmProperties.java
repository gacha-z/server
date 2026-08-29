package dk.gatchaz.server.interfaces.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * FCM(Firebase Cloud Messaging) HTTP v1 연동 설정.
 * 값은 Server-Secret 의 프로파일별 yml(fcm)에서 관리한다.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "fcm")
public class FcmProperties {

    /** Firebase 프로젝트 ID. 발송 URL 에 들어간다. */
    private String projectId;

    /**
     * Firebase 서비스 계정 키(JSON) 위치. 스프링 리소스 경로로 지정한다.
     * (예: file:/etc/secret/firebase.json, classpath:firebase.json)
     * 값이 비어 있으면 푸시 발송을 하지 않고 인앱 알림만 저장한다.
     */
    private String credentialsLocation;

    /** 연결 타임아웃 (ms) */
    private int connectTimeoutMs = 3000;

    /** 응답 대기 타임아웃 (ms) */
    private int readTimeoutMs = 5000;

    /**
     * 푸시를 실제로 보낼 수 있는 설정인지 확인한다.
     * 프로젝트 ID 와 서비스 계정 키가 모두 있어야 발송할 수 있다.
     */
    public boolean isSendable() {
        return StringUtils.hasText(projectId) && StringUtils.hasText(credentialsLocation);
    }
}
