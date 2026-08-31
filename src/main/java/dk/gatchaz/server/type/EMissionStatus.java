package dk.gatchaz.server.type;

/**
 * 팀이 선택해 진행 중인 미션(trip_mission)의 상태.
 * IN_PROGRESS - 선택되어 진행 중
 * COMPLETED   - 완료됨 (셋로그 + 위치 인증 통과)
 * FAILED      - 실패/포기 처리됨
 */
public enum EMissionStatus {
    IN_PROGRESS,
    COMPLETED,
    FAILED
}
