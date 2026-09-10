package dk.gatchaz.server.type;

/**
 * 팀이 선택해 진행 중인 미션(trip_mission)의 상태.
 * IN_PROGRESS   - 선택되어 진행 중
 * COMPLETED     - 완료됨 (셋로그 + 위치 인증 통과)
 * FAILED        - 실패/포기 처리됨 (진행 중이던 라운드가 처리되지 않은 채 날짜가 넘어간 경우 포함)
 * NOT_PERFORMED - 미션 수행 안함. 후보로 뽑히지도 못한 채 그 라운드의 날짜가 지나버려, 자동 마감 배치가
 *                 임의의 미션을 배정해 "이 라운드는 아예 진행되지 않았다"는 이력만 남긴 경우.
 */
public enum EMissionStatus {
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    NOT_PERFORMED
}
