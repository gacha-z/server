package dk.gatchaz.server.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 디바이스 권한 상태 응답.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "디바이스 권한 상태 응답")
public class DevicePermissionResponse {

    @Schema(description = "디바이스 권한 상태 ID", example = "1")
    private Long devicePermissionId;

    @Schema(description = "디바이스 ID", example = "1")
    private Long deviceId;

    @Schema(description = "위치 권한 상태", example = "GRANTED")
    private String locationStatus;

    @Schema(description = "카메라 권한 상태", example = "GRANTED")
    private String cameraStatus;

    @Schema(description = "알림 권한 상태", example = "DENIED")
    private String notificationStatus;

    @Schema(description = "마지막 갱신 시각")
    private LocalDateTime updatedAt;
}
