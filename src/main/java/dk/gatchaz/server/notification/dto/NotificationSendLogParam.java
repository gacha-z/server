package dk.gatchaz.server.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 기기별 푸시 발송 결과 기록.
 */
@Getter
@AllArgsConstructor
public class NotificationSendLogParam {

    private Long notificationId;
    private Long deviceId;

    /** ENotificationSendStatus 값 */
    private String sendStatus;

    /** 실패 사유. 성공이면 null */
    private String errorMessage;
}
