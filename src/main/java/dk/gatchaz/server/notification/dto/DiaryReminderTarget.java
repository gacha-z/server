package dk.gatchaz.server.notification.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 일기 작성 독려 알림을 받을 대상.
 * 여행 기간 중이면서 오늘 일기를 쓰지 않은 참여자 1명을 나타낸다.
 */
@Getter
@Setter
@NoArgsConstructor
public class DiaryReminderTarget {

    private Long memberId;

    /** 알림을 눌렀을 때 이동할 여행 ID */
    private Long tripId;

    /** 알림 제목으로 쓸 여행 이름 */
    private String tripTitle;
}
