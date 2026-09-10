package dk.gatchaz.server.type;

/**
 * 회원의 시스템 권한.
 * USER 는 일반 앱 사용자, ADMIN 은 배치/운영 API(예: /api/v1/batch/**) 를 포함해 모든 API 를 쓸 수 있다.
 * member 테이블 role 컬럼(ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER')과 이름이 그대로 매핑된다.
 */
public enum ERole {
    USER,
    ADMIN;

    /** Spring Security 의 hasRole(...)/authorizeHttpRequests 에서 쓰는 "ROLE_XXX" 형태 문자열. */
    public String toRoleName() {
        return "ROLE_" + name();
    }
}
