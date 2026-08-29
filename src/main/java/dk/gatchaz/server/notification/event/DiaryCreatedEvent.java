package dk.gatchaz.server.notification.event;

import dk.gatchaz.server.type.EDiaryVisibility;

/**
 * 일기가 저장되었음을 알리는 이벤트.
 * 일기 저장 트랜잭션이 커밋된 뒤에 처리되므로, 저장이 롤백되면 알림도 나가지 않는다.
 *
 * @param diaryId         저장된 일기 ID
 * @param tripId          일기가 속한 여행 ID
 * @param writerMemberId  작성자 회원 ID
 * @param visibility      공개 범위. 알림을 보낼지는 받는 쪽에서 판단한다.
 */
public record DiaryCreatedEvent(Long diaryId, Long tripId, Long writerMemberId, EDiaryVisibility visibility) {
}
