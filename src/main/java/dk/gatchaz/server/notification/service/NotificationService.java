package dk.gatchaz.server.notification.service;

import dk.gatchaz.server.interfaces.dto.FcmSendResult;
import dk.gatchaz.server.interfaces.support.FcmClient;
import dk.gatchaz.server.exception.CommonException;
import dk.gatchaz.server.exception.ErrorCode;
import dk.gatchaz.server.notification.dto.DevicePermissionResponse;
import dk.gatchaz.server.notification.dto.DevicePermissionSaveParam;
import dk.gatchaz.server.notification.dto.DevicePermissionUpdateRequest;
import dk.gatchaz.server.notification.dto.DeviceRegisterRequest;
import dk.gatchaz.server.notification.dto.DeviceRegisterResponse;
import dk.gatchaz.server.notification.dto.DeviceSaveParam;
import dk.gatchaz.server.notification.dto.DeviceTokenDto;
import dk.gatchaz.server.notification.dto.DiaryNotificationContext;
import dk.gatchaz.server.notification.dto.DiaryReminderTarget;
import dk.gatchaz.server.notification.dto.NotificationCreateParam;
import dk.gatchaz.server.notification.dto.NotificationListResponse;
import dk.gatchaz.server.notification.dto.NotificationResponse;
import dk.gatchaz.server.notification.dto.NotificationSearchParam;
import dk.gatchaz.server.notification.dto.NotificationSendLogParam;
import dk.gatchaz.server.notification.mapper.NotificationMapper;
import dk.gatchaz.server.type.ENotificationSendStatus;
import dk.gatchaz.server.type.ENotificationTargetType;
import dk.gatchaz.server.type.ENotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 알림 저장·조회와 푸시 발송을 담당한다.
 * 알림을 만드는 시점(어떤 사건에 보낼지)은 이벤트 리스너와 배치가 결정하고, 이 서비스는 만들고 보내는 일만 한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 50;

    private static final String YES = "Y";
    private static final String NO = "N";

    /** notification.title 컬럼 길이 */
    private static final int TITLE_MAX_LENGTH = 100;
    /** notification.body 컬럼 길이 */
    private static final int BODY_MAX_LENGTH = 500;

    private static final String DEFAULT_TRIP_TITLE = "여행";
    private static final String DEFAULT_NICKNAME = "팀원";
    private static final String DIARY_CREATED_BODY_SUFFIX = "님이 새 일기를 남겼어요.";
    private static final String DIARY_REMINDER_BODY = "오늘 하루는 어땠나요? 여행 일기를 남겨보세요.";
    private static final String FCM_NOT_CONFIGURED = "FCM 설정이 없어 발송하지 않았습니다.";

    private final NotificationMapper notificationMapper;
    private final FcmClient fcmClient;

    /**
     * 기기를 등록하거나 갱신한다.
     * 같은 FCM 토큰이 이미 있으면 그 기기의 소유자·OS·앱 버전·수신 여부를 갱신한다.
     * (기기를 재설치하거나 다른 계정으로 로그인해도 토큰이 같으면 같은 기기로 본다)
     */
    @Transactional
    public DeviceRegisterResponse registerDevice(final DeviceRegisterRequest request) {
        final DeviceSaveParam param = DeviceSaveParam.builder()
                .deviceId(notificationMapper.selectDeviceIdByFcmToken(request.getFcmToken()))
                .memberId(request.getMemberId())
                .fcmToken(request.getFcmToken())
                .osType(request.getOsType())
                .appVersion(request.getAppVersion())
                // 값을 주지 않으면 수신 동의로 저장한다.
                .notificationEnabled(Boolean.FALSE.equals(request.getNotificationEnabled()) ? NO : YES)
                .build();

        if (param.getDeviceId() == null) {
            notificationMapper.insertDevice(param);
        } else {
            notificationMapper.updateDevice(param);
        }
        return new DeviceRegisterResponse(param.getDeviceId());
    }

    /**
     * 디바이스(deviceId)의 권한 상태(위치/카메라/알림)를 갱신한다. 부분 업데이트 - 값을 보낸 필드만 반영한다.
     * 권한 상태 행이 아직 없으면(최초 호출) 새로 만들고, 있으면 갱신한다. 대상 디바이스가 없으면 예외를 던진다.
     */
    @Transactional
    public DevicePermissionResponse updateDevicePermission(final Long deviceId,
                                                            final DevicePermissionUpdateRequest request) {
        if (notificationMapper.countDeviceById(deviceId) == 0) {
            throw new CommonException(ErrorCode.NOT_FOUND_DEVICE);
        }

        final Long devicePermissionId = notificationMapper.selectDevicePermissionIdByDeviceId(deviceId);

        final DevicePermissionSaveParam param = DevicePermissionSaveParam.builder()
                .deviceId(deviceId)
                .locationStatus(request.getLocationStatus())
                .cameraStatus(request.getCameraStatus())
                .notificationStatus(request.getNotificationStatus())
                .build();

        if (devicePermissionId == null) {
            notificationMapper.insertDevicePermission(param);
        } else {
            notificationMapper.updateDevicePermission(param);
        }

        return notificationMapper.selectDevicePermissionByDeviceId(deviceId);
    }

    /**
     * 회원의 알림함을 최신순으로 조회한다. (커서 기반 무한 스크롤)
     * hasNext 판별을 위해 요청 개수 + 1 을 조회한 뒤 초과분을 잘라낸다.
     */
    @Transactional(readOnly = true)
    public NotificationListResponse getNotifications(final Long memberId, final Long cursor, final int size) {
        final int pageSize = Math.min(Math.max(size, MIN_SIZE), MAX_SIZE);

        final List<NotificationResponse> notifications =
                notificationMapper.selectNotifications(new NotificationSearchParam(memberId, cursor, pageSize + 1));

        final boolean hasNext = notifications.size() > pageSize;
        if (hasNext) {
            notifications.remove(notifications.size() - 1);
        }
        final Long nextCursor = hasNext
                ? notifications.get(notifications.size() - 1).getNotificationId()
                : null;

        return new NotificationListResponse(
                notifications, nextCursor, hasNext, notificationMapper.countUnreadNotifications(memberId));
    }

    /**
     * 알림 1건을 읽음 처리한다.
     * 본인 알림이 아니거나 이미 읽은 알림이면 아무것도 바꾸지 않는다. (중복 호출해도 안전)
     */
    @Transactional
    public void readNotification(final Long notificationId, final Long memberId) {
        notificationMapper.markNotificationRead(notificationId, memberId);
    }

    /**
     * 팀원이 일기를 작성했음을 같은 여행의 다른 참여자에게 알린다.
     * 일기 저장 트랜잭션이 커밋된 뒤 별도 스레드에서 호출된다.
     */
    public void notifyDiaryCreated(final Long diaryId, final Long tripId, final Long writerMemberId) {
        final List<Long> targetMemberIds = notificationMapper.selectJoinedMemberIdsExcept(tripId, writerMemberId);
        if (targetMemberIds.isEmpty()) {
            return;
        }

        final DiaryNotificationContext context = notificationMapper.selectDiaryNotificationContext(diaryId);
        final String title = tripTitle(context == null ? null : context.getTripTitle());
        final String body = diaryCreatedBody(context);

        final List<NotificationCreateParam> params = new ArrayList<>(targetMemberIds.size());
        for (final Long memberId : targetMemberIds) {
            params.add(NotificationCreateParam.builder()
                    .memberId(memberId)
                    .type(ENotificationType.DIARY_CREATED.name())
                    .title(title)
                    .body(body)
                    .targetType(ENotificationTargetType.DIARY.name())
                    .targetId(diaryId)
                    .build());
        }
        createAndPush(params);
    }

    /**
     * 여행 기간 중이면서 오늘 일기를 아직 쓰지 않은 참여자에게 작성을 권유한다. (매일 저녁 배치)
     * 알림을 보낸 회원 수를 반환한다.
     *
     * 회원마다 여행이 다르므로 제목과 이동 대상(target_id)도 회원별로 달라진다.
     */
    public int notifyDiaryWriteReminder() {
        final List<DiaryReminderTarget> targets = notificationMapper.selectDiaryReminderTargets();
        if (targets.isEmpty()) {
            return 0;
        }

        final List<NotificationCreateParam> params = new ArrayList<>(targets.size());
        for (final DiaryReminderTarget target : targets) {
            params.add(NotificationCreateParam.builder()
                    .memberId(target.getMemberId())
                    .type(ENotificationType.DIARY_WRITE_REMINDER.name())
                    .title(tripTitle(target.getTripTitle()))
                    .body(DIARY_REMINDER_BODY)
                    // 일기 작성 화면으로 보내야 하므로 여행을 대상으로 한다.
                    .targetType(ENotificationTargetType.TRIP.name())
                    .targetId(target.getTripId())
                    .build());
        }
        createAndPush(params);
        return params.size();
    }

    /**
     * 알림을 저장하고, 알림 수신을 켠 기기에 푸시를 보낸 뒤 기기별 결과를 기록한다.
     *
     * 외부(FCM) 호출이 포함되므로 전체를 하나의 트랜잭션으로 묶지 않는다.
     * 외부 호출 시간 동안 커넥션을 점유하지 않기 위함이며, 알림 INSERT 는 각각 단일 문장이라 그 자체로 원자적이다.
     * 한 기기가 실패해도 나머지 기기 발송은 계속한다.
     */
    private void createAndPush(final List<NotificationCreateParam> params) {
        // 1. 인앱 알림을 저장한다. (푸시 발송 여부와 무관하게 알림함에는 항상 남는다)
        final Map<Long, NotificationCreateParam> byMember = new LinkedHashMap<>(params.size());
        for (final NotificationCreateParam param : params) {
            notificationMapper.insertNotification(param);
            byMember.put(param.getMemberId(), param);
        }

        // 2. 알림 수신을 켠 기기에 푸시를 보낸다.
        final List<DeviceTokenDto> devices =
                notificationMapper.selectSendableDevices(List.copyOf(byMember.keySet()));
        if (devices.isEmpty()) {
            return;
        }

        final boolean sendable = fcmClient.isSendable();
        if (!sendable) {
            log.warn("FCM 설정이 없어 푸시를 보내지 않았습니다. 인앱 알림만 저장됩니다. (대상 기기 {}대)", devices.size());
        }

        final List<NotificationSendLogParam> logs = new ArrayList<>(devices.size());
        for (final DeviceTokenDto device : devices) {
            final NotificationCreateParam param = byMember.get(device.getMemberId());
            if (param == null) {
                continue;
            }
            if (!sendable) {
                logs.add(new NotificationSendLogParam(param.getNotificationId(), device.getDeviceId(),
                        ENotificationSendStatus.SKIPPED.name(), FCM_NOT_CONFIGURED));
                continue;
            }
            final FcmSendResult result =
                    fcmClient.send(device.getFcmToken(), param.getTitle(), param.getBody(), pushData(param));
            logs.add(new NotificationSendLogParam(param.getNotificationId(), device.getDeviceId(),
                    result.success() ? ENotificationSendStatus.SUCCESS.name() : ENotificationSendStatus.FAIL.name(),
                    result.errorMessage()));
        }

        if (!logs.isEmpty()) {
            notificationMapper.insertNotificationSendLogs(logs);
        }
    }

    /**
     * 앱이 알림을 눌렀을 때 이동할 화면을 판단하는 데 쓴다. FCM data 값은 모두 문자열이어야 한다.
     */
    private Map<String, String> pushData(final NotificationCreateParam param) {
        return Map.of(
                "type", param.getType(),
                "targetType", param.getTargetType(),
                "targetId", String.valueOf(param.getTargetId()));
    }

    /** 알림 제목은 여행 이름을 쓴다. 컬럼 길이를 넘지 않도록 자른다. */
    private String tripTitle(final String tripTitle) {
        return truncate(StringUtils.hasText(tripTitle) ? tripTitle : DEFAULT_TRIP_TITLE, TITLE_MAX_LENGTH);
    }

    private String diaryCreatedBody(final DiaryNotificationContext context) {
        final String nickname = context == null || !StringUtils.hasText(context.getWriterNickname())
                ? DEFAULT_NICKNAME
                : context.getWriterNickname();
        return truncate(nickname + DIARY_CREATED_BODY_SUFFIX, BODY_MAX_LENGTH);
    }

    private String truncate(final String value, final int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
