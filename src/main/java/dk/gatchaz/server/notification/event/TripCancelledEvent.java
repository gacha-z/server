package dk.gatchaz.server.notification.event;

/**
 * 여행(trip)이 취소되었음을 알리는 이벤트. (status 'CREATED' -> 'CANCELLED')
 * 여행 기록과 그동안의 미션 수행 이력 자체는 그대로 남기되, 이 여행에서 얻은 배지 진행도(미션 성공/FOOD/CAFE/일기)는
 * 취소된 순간 되돌려야 하므로 발행한다. 취소 처리 트랜잭션이 커밋된 뒤에 처리되므로, 취소가 롤백되면 배지 되돌리기도
 * 나가지 않는다.
 *
 * 여행 완료(TripCompletedEvent) 때만 지급되는 여행 횟수/지역별/지역 탐험 배지와 지역 아이템은 취소된 여행에서는
 * 애초에 지급된 적이 없으므로(취소는 완료 이전 상태에서만 가능) 되돌릴 필요가 없다.
 *
 * @param tripId 취소된 여행 ID
 */
public record TripCancelledEvent(Long tripId) {
}
