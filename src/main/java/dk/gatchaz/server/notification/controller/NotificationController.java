package dk.gatchaz.server.notification.controller;

import dk.gatchaz.server.common.annotation.UserId;
import dk.gatchaz.server.common.dto.ResponseDto;
import dk.gatchaz.server.notification.dto.NotificationListResponse;
import dk.gatchaz.server.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notification", description = "알림 API")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 알림함 조회
     */
    @Operation(
            summary = "알림함 조회",
            description = """
                    회원이 받은 알림을 최신순으로 조회한다. (커서 기반 무한 스크롤)

                    ### 무한 스크롤 (커서 방식)
                    1. 첫 조회는 `cursor` 없이 호출한다.
                    2. 응답의 `nextCursor` 를 그대로 다음 요청의 `cursor` 로 넘긴다.
                    3. `hasNext = false` (이때 `nextCursor = null`) 이면 마지막 페이지이므로 추가 호출을 멈춘다.

                    ### 알림을 눌렀을 때
                    - `targetType` 과 `targetId` 로 이동할 화면을 결정한다. (예: `DIARY` + `5` → 일기 상세 5번)
                    - 같은 값이 푸시 메시지의 `data` 에도 담겨 오므로, 푸시를 눌러 들어온 경우와 동일하게 처리하면 된다.

                    ### 뱃지 표시
                    - `unreadCount` 는 커서와 무관하게 **항상 전체 기준**으로 계산된 읽지 않은 알림 수다.
                      페이지를 넘겨도 값이 달라지지 않으므로 그대로 뱃지에 쓰면 된다.

                    ### 알림 종류 (`type`)
                    - `DIARY_CREATED`: 같은 여행의 팀원이 일기를 작성함
                    """)
    @GetMapping
    public ResponseDto<NotificationListResponse> getNotifications(
            @UserId final Long userId,
            @Parameter(description = "무한 스크롤 커서. 이전 응답의 nextCursor 값을 넣는다. 첫 조회 시 비운다.", example = "12")
            @RequestParam(required = false) final Long cursor,
            @Parameter(description = "한 번에 조회할 개수 (기본 20, 1~50 범위를 벗어나면 자동 보정)", example = "20")
            @RequestParam(required = false, defaultValue = "20") final int size) {
        return ResponseDto.ok(notificationService.getNotifications(userId, cursor, size));
    }

    /**
     * 알림 읽음 처리
     */
    @Operation(
            summary = "알림 읽음 처리",
            description = """
                    알림 1건을 읽음으로 표시한다. 목록에서 알림을 눌렀을 때 호출한다.

                    ### 참고
                    - 본인 알림이 아니거나 이미 읽은 알림이면 아무것도 바뀌지 않는다. **중복 호출해도 안전하다.**
                    - 성공/무시 모두 200 을 반환한다. 갱신된 읽지 않은 수는 알림함 조회의 `unreadCount` 로 확인한다.
                    """)
    @PatchMapping("/{notificationId}/read")
    public ResponseDto<Void> readNotification(
            @Parameter(description = "읽음 처리할 알림 ID", example = "12") @PathVariable final Long notificationId,
            @UserId final Long userId) {
        notificationService.readNotification(notificationId, userId);
        return ResponseDto.<Void>ok(null);
    }
}
