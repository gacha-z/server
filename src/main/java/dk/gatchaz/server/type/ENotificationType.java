package dk.gatchaz.server.type;

/**
 * 알림 종류. (notification.type)
 * DIARY_CREATED         - 같은 여행의 팀원이 일기를 작성함 (일기 저장 시점에 발송)
 * DIARY_WRITE_REMINDER  - 오늘 일기를 아직 쓰지 않은 팀원에게 작성을 권유함 (여행 기간 중 매일 저녁 배치)
 *
 * 미션 기능 개발 후 미션 시작/다음 미션 알림 타입을 여기에 추가한다.
 */
public enum ENotificationType {
    DIARY_CREATED,
    DIARY_WRITE_REMINDER
}
