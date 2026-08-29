package dk.gatchaz.server.notification.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 알림 저장 파라미터. INSERT 후 생성된 notificationId 가 채워진다.
 */
@Getter
@Setter
@NoArgsConstructor
@Builder
@lombok.AllArgsConstructor
public class NotificationCreateParam {

    private Long notificationId;
    private Long memberId;
    private String type;
    private String title;
    private String body;
    private String targetType;
    private Long targetId;
}
