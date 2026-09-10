package dk.gatchaz.server.notification.event;

/**
 * 여행(trip)이 완료 처리되었음을 알리는 이벤트.
 * 여행의 마지막 날 마지막 라운드 미션까지 완료/포기로 끝나 trip.status 가 'COMPLETED' 로 바뀐 직후 발행된다.
 * 완료 처리 트랜잭션이 커밋된 뒤에 처리되므로, 완료가 롤백되면 배지/아이템 지급도 나가지 않는다.
 *
 * @param tripId 완료된 여행 ID
 */
public record TripCompletedEvent(Long tripId) {
}
