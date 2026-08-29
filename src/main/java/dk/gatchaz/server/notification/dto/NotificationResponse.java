package dk.gatchaz.server.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 알림함의 알림 1건.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "알림 1건")
public class NotificationResponse {

    @Schema(description = "알림 ID. 읽음 처리 API 에 사용한다.", example = "12")
    private Long notificationId;

    @Schema(description = "알림 종류 (DIARY_CREATED: 팀원이 일기를 작성함)", example = "DIARY_CREATED")
    private String type;

    @Schema(description = "알림 제목. 보통 여행 이름이다.", example = "제주 여행")
    private String title;

    @Schema(description = "알림 본문", example = "여행러버님이 새 일기를 남겼어요.")
    private String body;

    @Schema(description = "알림을 눌렀을 때 이동할 대상 종류 (DIARY: 일기 상세)", example = "DIARY")
    private String targetType;

    @Schema(description = "이동할 대상의 ID. targetType 과 함께 사용한다.", example = "5")
    private Long targetId;

    @Schema(description = "읽음 여부", example = "false")
    private boolean read;

    @Schema(description = "알림 생성 일시", example = "2026-08-29T19:00:00")
    private LocalDateTime createdAt;
}
