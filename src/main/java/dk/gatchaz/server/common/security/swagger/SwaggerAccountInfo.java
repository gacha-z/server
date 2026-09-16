package dk.gatchaz.server.common.security.swagger;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Swagger UI/API 문서 열람 전용 계정 1건. 앱 회원(member) 시스템과는 완전히 별개다.
 */
@Getter
@Setter
@NoArgsConstructor
public class SwaggerAccountInfo {

    private String username;

    /** BCrypt 해시 */
    private String password;
}
