package dk.gatchaz.server.notification.event;

/**
 * 미션(trip_mission)이 완료되었음을 알리는 이벤트.
 * 완료 처리 트랜잭션이 커밋된 뒤에 처리되므로, 완료가 롤백되면 배지 지급도 나가지 않는다.
 *
 * @param tripId        미션이 속한 여행 ID
 * @param tripMissionId 완료된 trip_mission ID
 * @param missionType   완료된 미션의 유형 (예: FOOD, CAFE, PHOTO 등)
 */
public record MissionCompletedEvent(Long tripId, Long tripMissionId, String missionType) {
}
