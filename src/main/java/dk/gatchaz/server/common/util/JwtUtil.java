package dk.gatchaz.server.common.util;

import dk.gatchaz.server.common.constant.Constant;
import dk.gatchaz.server.auth.dto.JwtTokenDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * 액세스/리프레시 토큰(JWT) 발급 및 검증. 클레임에는 회원 ID(memberId) 하나만 담는다(gatchaz 는 Role 개념 없음).
 * (참고 코드는 jwt.secret 값을 Base64 로 디코딩해서 서명 키로 썼는데, application-*.yml 에 이미 채워져 있던
 * 실제 secret 값은 Base64 로 디코딩이 안 되는 평문 문자열이었다(패딩 오류). 그래서 여기서는 그 문자열을
 * UTF-8 바이트 그대로 서명 키로 쓰도록 고쳤다 - 길이도 86바이트라 HS512 최소 요구치(64바이트)를 충분히 만족한다.)
 */
@Slf4j
@Component
public class JwtUtil implements InitializingBean {

    @Value("${jwt.access-expiration-ms}")
    private long accessExpirationMs;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Value("${jwt.secret}")
    private String secretKey;

    private Key key;

    @Override
    public void afterPropertiesSet() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 액세스/리프레시 토큰을 한 쌍으로 발급한다. (로그인, 토큰 재발급 시 사용)
     */
    public JwtTokenDto generateTokens(Long memberId) {
        return new JwtTokenDto(generateAccessToken(memberId), generateRefreshToken(memberId));
    }

    public String generateAccessToken(Long memberId) {
        return generateToken(memberId, accessExpirationMs);
    }

    public String generateRefreshToken(Long memberId) {
        return generateToken(memberId, refreshExpirationMs);
    }

    private String generateToken(Long memberId, long expirationMs) {
        final Claims claims = Jwts.claims();
        claims.put(Constant.USER_ID_CLAIM_NAME, memberId.toString());

        final Date now = new Date();
        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationMs))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public Claims validateToken(String token) throws JwtException {
        final JwtParser jwtParser = Jwts.parserBuilder().setSigningKey(key).build();
        return jwtParser.parseClaimsJws(token).getBody();
    }

    public long getAccessExpirationMs() {
        return accessExpirationMs;
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }
}
