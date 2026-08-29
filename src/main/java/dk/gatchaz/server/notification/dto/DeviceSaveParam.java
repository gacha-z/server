package dk.gatchaz.server.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 기기 저장 파라미터. INSERT 후 생성된 deviceId 가 채워진다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceSaveParam {

    private Long deviceId;
    private Long memberId;
    private String fcmToken;
    private String osType;
    private String appVersion;

    /** 'Y' 또는 'N' */
    private String notificationEnabled;
}
