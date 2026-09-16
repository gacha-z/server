package dk.gatchaz.server.notification.dto;

import dk.gatchaz.server.type.EPermissionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 디바이스 권한 상태 갱신 요청. 부분 업데이트 - 값을 보낸 필드만 갱신하고, 나머지는 기존 값을 유지한다.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "디바이스 권한 상태 갱신 요청 (부분 업데이트, 값이 있는 필드만 반영)")
public class DevicePermissionUpdateRequest {

    @Schema(description = "위치 권한 상태(선택)", example = "GRANTED")
    private EPermissionStatus locationStatus;

    @Schema(description = "카메라 권한 상태(선택)", example = "GRANTED")
    private EPermissionStatus cameraStatus;

    @Schema(description = "알림 권한 상태(선택)", example = "DENIED")
    private EPermissionStatus notificationStatus;
}
