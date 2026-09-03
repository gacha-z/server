package dk.gatchaz.server.type;

/**
 * 알림을 눌렀을 때 이동할 대상의 종류. (notification.target_type)
 * 대상 식별자는 notification.target_id 에 담긴다.
 * DIARY - 일기 상세 (target_id = diary_id)
 * TRIP  - 해당 여행의 일기 작성 화면 (target_id = trip_id)
 */
public enum ENotificationTargetType {
    DIARY,
    TRIP
}
