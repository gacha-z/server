package dk.gatchaz.server.notification.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 푸시 발송 대상 기기. (알림 수신을 켠 기기만 조회된다)
 */
@Getter
@Setter
@NoArgsConstructor
public class DeviceTokenDto {

    private Long deviceId;
    private Long memberId;
    private String fcmToken;
}
