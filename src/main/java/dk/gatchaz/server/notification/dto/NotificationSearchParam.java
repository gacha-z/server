package dk.gatchaz.server.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 알림함 조회 조건.
 */
@Getter
@AllArgsConstructor
public class NotificationSearchParam {

    private Long memberId;

    /** 이 값보다 작은 notification_id 만 조회한다. 첫 조회면 null. */
    private Long cursor;

    /** hasNext 판별을 위해 요청 개수 + 1 이 들어온다. */
    private int size;
}
