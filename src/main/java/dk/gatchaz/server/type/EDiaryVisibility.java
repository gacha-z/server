package dk.gatchaz.server.type;

/**
 * 일기 공개 범위.
 * PRIVATE - 작성자 본인만
 * TEAM    - 같은 여행 참여자에게 공개 (기본값)
 * PUBLIC  - 전체 공개
 */
public enum EDiaryVisibility {
    PRIVATE,
    TEAM,
    PUBLIC
}
