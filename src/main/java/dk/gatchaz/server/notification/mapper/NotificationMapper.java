package dk.gatchaz.server.notification.mapper;

import dk.gatchaz.server.notification.dto.DevicePermissionResponse;
import dk.gatchaz.server.notification.dto.DevicePermissionSaveParam;
import dk.gatchaz.server.notification.dto.DeviceSaveParam;
import dk.gatchaz.server.notification.dto.DeviceTokenDto;
import dk.gatchaz.server.notification.dto.DiaryNotificationContext;
import dk.gatchaz.server.notification.dto.DiaryReminderTarget;
import dk.gatchaz.server.notification.dto.NotificationCreateParam;
import dk.gatchaz.server.notification.dto.NotificationResponse;
import dk.gatchaz.server.notification.dto.NotificationSearchParam;
import dk.gatchaz.server.notification.dto.NotificationSendLogParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper {

    /**
     * FCM 토큰으로 이미 등록된 기기 ID 를 조회한다. 없으면 null.
     * 기기를 재설치하거나 계정을 바꿔도 토큰이 같으면 같은 기기로 본다.
     */
    Long selectDeviceIdByFcmToken(@Param("fcmToken") String fcmToken);

    /**
     * 기기를 새로 등록한다. 생성된 device_id 는 param.deviceId 에 채워진다.
     */
    int insertDevice(DeviceSaveParam param);

    /**
     * 이미 등록된 기기의 소유자·OS·앱 버전·수신 여부를 갱신하고 최근 접속 시각을 기록한다.
     */
    int updateDevice(DeviceSaveParam param);

    /**
     * 디바이스(deviceId)의 소유자 회원 ID 를 조회한다. 없으면 null.
     */
    Long selectDeviceMemberId(@Param("deviceId") Long deviceId);

    /**
     * 디바이스(deviceId)의 권한 상태(device_permission) 행 ID 를 조회한다. 없으면 null.
     */
    Long selectDevicePermissionIdByDeviceId(@Param("deviceId") Long deviceId);

    /**
     * 디바이스(deviceId)의 권한 상태를 단건 조회한다. 없으면 null.
     */
    DevicePermissionResponse selectDevicePermissionByDeviceId(@Param("deviceId") Long deviceId);

    /**
     * 디바이스 권한 상태를 새로 등록한다. 생성된 device_permission_id 는 param.devicePermissionId 에 채워진다.
     */
    int insertDevicePermission(DevicePermissionSaveParam param);

    /**
     * 디바이스 권한 상태를 부분 갱신한다. (값이 있는 필드만 갱신, 나머지는 기존 값 유지)
     */
    int updateDevicePermission(DevicePermissionSaveParam param);

    /**
     * 알림함 목록을 최신순(notification_id 내림차순)으로 조회한다.
     * hasNext 판별을 위해 서비스에서 요청 개수 + 1 을 size 로 넘긴다.
     */
    List<NotificationResponse> selectNotifications(NotificationSearchParam param);

    /**
     * 회원의 읽지 않은 알림 수를 조회한다. (뱃지 표시용)
     */
    int countUnreadNotifications(@Param("memberId") Long memberId);

    /**
     * 알림 1건을 읽음 처리한다. 본인 알림이 아니거나 이미 읽었으면 0 을 반환한다.
     */
    int markNotificationRead(@Param("notificationId") Long notificationId, @Param("memberId") Long memberId);

    /**
     * 알림을 저장한다. 생성된 notification_id 는 param.notificationId 에 채워진다.
     */
    int insertNotification(NotificationCreateParam param);

    /**
     * 푸시를 보낼 기기 목록을 조회한다.
     * 알림 수신을 켜고(notification_enabled = 'Y') FCM 토큰이 있는 기기만 대상으로 한다.
     * memberIds 는 비어 있지 않아야 한다. (호출 전에 확인)
     */
    List<DeviceTokenDto> selectSendableDevices(@Param("memberIds") List<Long> memberIds);

    /**
     * 기기별 발송 결과를 한 번에 저장한다. logs 는 비어 있지 않아야 한다. (호출 전에 확인)
     */
    int insertNotificationSendLogs(@Param("logs") List<NotificationSendLogParam> logs);

    /**
     * 일기 작성 알림 문구에 쓸 여행 이름과 작성자 닉네임을 조회한다. 일기가 없으면 null.
     */
    DiaryNotificationContext selectDiaryNotificationContext(@Param("diaryId") Long diaryId);

    /**
     * 해당 여행에 참여(JOINED) 중인 팀원 중 지정한 회원(exceptMemberId)을 뺀 회원 ID 목록을 조회한다.
     * (일기를 쓴 본인에게는 알림을 보내지 않는다)
     */
    List<Long> selectJoinedMemberIdsExcept(@Param("tripId") Long tripId,
                                           @Param("exceptMemberId") Long exceptMemberId);

    /**
     * 일기 작성 독려 알림 대상을 조회한다.
     * 진행 중인 여행(오늘이 여행 기간 안이고 취소·완료되지 않음)에 참여(JOINED) 중이면서
     * 오늘 일기를 아직 쓰지 않은 회원만 대상으로 한다.
     *
     * 일기는 회원 기준 하루 1개만 쓸 수 있으므로, 한 회원이 같은 기간에 여러 여행에 참여 중이어도
     * 여행 1개(trip_id 가 가장 작은 것)만 골라 알림이 중복되지 않게 한다.
     */
    List<DiaryReminderTarget> selectDiaryReminderTargets();
}
