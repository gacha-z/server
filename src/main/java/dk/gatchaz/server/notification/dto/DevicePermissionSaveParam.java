package dk.gatchaz.server.notification.dto;

import dk.gatchaz.server.type.EPermissionStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 디바이스 권한 상태 등록(INSERT)/갱신(UPDATE) 공용 파라미터.
 * INSERT 시 생성된 devicePermissionId 가 채워진다.
 */
@Getter
@Setter
@Builder
public class DevicePermissionSaveParam {

    private Long devicePermissionId;
    private Long deviceId;
    private EPermissionStatus locationStatus;
    private EPermissionStatus cameraStatus;
    private EPermissionStatus notificationStatus;
}
