package dk.gatchaz.server.interfaces.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import dk.gatchaz.server.interfaces.config.FcmProperties;
import dk.gatchaz.server.interfaces.dto.FcmSendResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.InputStream;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * FCM(Firebase Cloud Messaging) HTTP v1 API 로 푸시를 발송한다.
 * 인증 토큰은 이미 클래스패스에 있는 google-auth-library 로 서비스 계정 키에서 발급받는다. (별도 SDK 의존성 없음)
 *
 * 서비스 계정 키가 설정되지 않았으면 발송하지 않는다. (isSendable() 이 false)
 * 이 경우에도 인앱 알림은 정상 저장되며, 키를 yml 에 넣으면 별도 코드 수정 없이 발송이 시작된다.
 */
@Slf4j
@Component
public class FcmClient {

    private static final String FCM_SEND_URL = "https://fcm.googleapis.com/v1/projects/%s/messages:send";
    private static final String MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";

    /** 발송 실패 사유를 기록할 때의 최대 길이 */
    private static final int ERROR_MESSAGE_LIMIT = 1000;

    private final FcmProperties properties;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    /** 서비스 계정 키는 처음 발송할 때 한 번만 읽는다. 토큰 캐시·갱신은 GoogleCredentials 가 처리한다. */
    private GoogleCredentials credentials;

    public FcmClient(final FcmProperties properties,
                     final ResourceLoader resourceLoader,
                     final ObjectMapper objectMapper,
                     final RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;

        final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()));
        requestFactory.setReadTimeout(Duration.ofMillis(properties.getReadTimeoutMs()));
        this.restClient = restClientBuilder.requestFactory(requestFactory).build();
    }

    /**
     * 푸시를 실제로 발송할 수 있는 설정인지 확인한다.
     * false 면 호출부는 발송을 건너뛰고 인앱 알림만 남긴다.
     */
    public boolean isSendable() {
        return properties.isSendable();
    }

    /**
     * 기기 1대에 푸시를 발송한다. 예외를 던지지 않고 결과 객체로 성공·실패를 반환한다.
     * (여러 기기에 보내는 도중 한 대가 실패해도 나머지 발송이 멈추지 않도록)
     *
     * @param data 앱이 알림을 눌렀을 때 이동할 화면을 판단하기 위한 값. (알림 종류, 대상 ID 등)
     */
    public FcmSendResult send(final String fcmToken, final String title, final String body,
                              final Map<String, String> data) {
        if (!isSendable()) {
            return FcmSendResult.fail("FCM 설정(projectId, credentialsLocation)이 없어 발송하지 않았습니다.");
        }

        try {
            final String accessToken = accessToken();
            final String payload = buildPayload(fcmToken, title, body, data);

            restClient.post()
                    .uri(String.format(FCM_SEND_URL, properties.getProjectId()))
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            return FcmSendResult.ok();
        } catch (final Exception e) {
            // 만료·삭제된 토큰(UNREGISTERED)도 여기로 들어온다. 사유를 그대로 남겨 발송 로그에서 확인한다.
            log.warn("FCM 발송 실패: message={}", e.getMessage());
            return FcmSendResult.fail(abbreviate(e.getMessage()));
        }
    }

    /**
     * 서비스 계정 키로 발급한 액세스 토큰을 반환한다. 만료되었으면 갱신한다.
     */
    private synchronized String accessToken() throws Exception {
        if (credentials == null) {
            try (InputStream in = resourceLoader.getResource(properties.getCredentialsLocation()).getInputStream()) {
                credentials = GoogleCredentials.fromStream(in).createScoped(MESSAGING_SCOPE);
            }
        }
        credentials.refreshIfExpired();
        return credentials.getAccessToken().getTokenValue();
    }

    /**
     * FCM HTTP v1 요청 본문을 만든다. 제목·본문에 따옴표가 들어가도 안전하도록 직접 문자열을 조립하지 않는다.
     */
    private String buildPayload(final String fcmToken, final String title, final String body,
                                final Map<String, String> data) throws Exception {
        final Map<String, Object> message = new LinkedHashMap<>();
        message.put("token", fcmToken);
        message.put("notification", Map.of("title", title, "body", body));
        if (data != null && !data.isEmpty()) {
            message.put("data", data);
        }
        return objectMapper.writeValueAsString(Map.of("message", message));
    }

    private String abbreviate(final String message) {
        if (message == null) {
            return "알 수 없는 오류";
        }
        return message.length() <= ERROR_MESSAGE_LIMIT ? message : message.substring(0, ERROR_MESSAGE_LIMIT);
    }
}
