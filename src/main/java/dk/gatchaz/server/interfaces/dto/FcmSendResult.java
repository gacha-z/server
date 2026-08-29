package dk.gatchaz.server.interfaces.dto;

/**
 * FCM 기기 1대에 대한 발송 결과.
 *
 * @param success      발송 성공 여부
 * @param errorMessage 실패 사유. 성공이면 null
 */
public record FcmSendResult(boolean success, String errorMessage) {

    public static FcmSendResult ok() {
        return new FcmSendResult(true, null);
    }

    public static FcmSendResult fail(final String errorMessage) {
        return new FcmSendResult(false, errorMessage);
    }
}
