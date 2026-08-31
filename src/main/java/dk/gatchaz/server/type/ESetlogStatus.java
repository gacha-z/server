package dk.gatchaz.server.type;

/**
 * 셋로그 상태.
 * ACTIVE  - 정상
 * DELETED - 삭제됨 (소프트 삭제, 현재 API 로는 노출하지 않음)
 */
public enum ESetlogStatus {
    ACTIVE,
    DELETED
}
