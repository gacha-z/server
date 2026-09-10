package dk.gatchaz.server.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 기기 등록/갱신 요청. 앱이 FCM 토큰을 발급받은 뒤와 토큰이 갱신될 때마다 호출한다.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "기기 등록/갱신 요청")
public class DeviceRegisterRequest {

    @Schema(description = "FCM 등록 토큰", example = "fMEP0vJq...")
    @NotBlank(message = "fcmToken 은 필수입니다.")
    private String fcmToken;

    @Schema(description = "기기 OS (IOS / ANDROID)", example = "IOS")
    private String osType;

    @Schema(description = "앱 버전", example = "1.0.0")
    private String appVersion;

    @Schema(description = "앱 내 알림 수신 동의 여부. 값이 없으면 수신(true)으로 저장한다.", example = "true")
    private Boolean notificationEnabled;
}
