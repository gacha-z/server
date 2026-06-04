package your.newproject.rename.constant;

import java.util.List;

public class Constant {
    public static final String USER_ID_CLAIM_NAME = "uid";
    public static final String USER_ROLE_CLAIM_NAME = "rol";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String AUTHORIZATION = "accessToken";
    public static final String REAUTHORIZATION = "refreshToken";

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final List<String> NO_NEED_AUTH_URLS = List.of(
            "/login/oauth2/code/google",
            "/oauth2/authorization/google",
            "/login/oauth2/code/apple",
            "/oauth2/authorization/apple",
            "/api/v1/auth/login/apple",
            "/api/v1/auth/login/google",
            "/api/v1/auth/refresh",
            "/favicon.ico"
    );
}
