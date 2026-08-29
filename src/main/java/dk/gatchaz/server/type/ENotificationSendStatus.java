package dk.gatchaz.server.type;

/**
 * 기기별 푸시 발송 결과. (notification_send_log.send_status)
 * SUCCESS - 발송 성공
 * FAIL    - 발송 실패 (사유는 error_message 에 기록)
 * SKIPPED - 발송하지 않음 (FCM 미설정 등)
 */
public enum ENotificationSendStatus {
    SUCCESS,
    FAIL,
    SKIPPED
}
