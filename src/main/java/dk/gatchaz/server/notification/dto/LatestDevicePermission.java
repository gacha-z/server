package dk.gatchaz.server.notification.dto;

import dk.gatchaz.server.type.EPermissionStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * 회원의 가장 최근 활동 디바이스(last_active_at 기준) 1건의 권한 상태.
 * 등록된 디바이스가 없거나 권한 상태가 아직 없으면 조회 결과 자체가 null 이다.
 */
@Getter
@Setter
public class LatestDevicePermission {

    private EPermissionStatus locationStatus;

    private EPermissionStatus cameraStatus;

    private EPermissionStatus notificationStatus;
}
