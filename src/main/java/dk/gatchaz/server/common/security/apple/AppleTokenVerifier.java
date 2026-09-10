package dk.gatchaz.server.common.security.apple;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import dk.gatchaz.server.common.exception.CommonException;
import dk.gatchaz.server.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * Apple Sign-In 으로 앱이 발급받은 identityToken 을 서버가 직접 검증한다.
 * Apple 인가 코드 교환(웹 리다이렉트)용 client-secret(JWT) 생성 로직은 앱 전용 로그인에서는
 * 필요 없어서 포팅하지 않았다 (참고 코드의 CustomClientSecretGenerator 에 해당).
 *
 * 참고 코드 대비 고친 점:
 * (1) aud(오디언스) 검증이 아예 빠져 있었다 - 서명/발급자(iss)/만료만 확인하고 있어서, 다른 Apple 앱이
 *    자기 identityToken(같은 Apple 계정으로도 발급 가능)을 여기로 보내도 통과되는 구멍이 있었다.
 *    이 프로젝트의 Apple client-id(Services ID)와 claim 의 aud 를 비교하는 검증을 추가함.
 * (2) 참고 코드는 "apple.audience" 프로퍼티에 issuer 값("https://appleid.apple.com")을 넣고 그걸로
 *    iss 를 검증하고 있었다 - 프로퍼티 이름과 실제 용도가 반대였음. 여기서는 oauth2.apple.issuer 로 바로잡음.
 */
@Component
@Slf4j
public class AppleTokenVerifier {

    @Value("${oauth2.apple.client-id}")
    private String clientId;

    @Value("${oauth2.apple.issuer}")
    private String issuer;

    @Value("${oauth2.apple.public-key-url}")
    private String publicKeyUrl;

    /**
     * 서명/발급자/audience/만료를 모두 검증한 뒤, Apple 이 부여한 사용자 식별자(sub 클레임)를 반환한다.
     * 이 값을 social_account.provider_user_id 에 저장해서 다음 로그인 때 같은 회원으로 매칭한다.
     */
    public String verifyAndGetProviderUserId(String identityToken) {
        try {
            final SignedJWT signedJWT = SignedJWT.parse(identityToken);

            final JWKSet jwkSet = JWKSet.load(URI.create(publicKeyUrl).toURL());
            final String kid = signedJWT.getHeader().getKeyID();
            final String alg = signedJWT.getHeader().getAlgorithm().getName();

            final List<JWK> keys = jwkSet.getKeys();
            final JWK jwk = keys.stream()
                    .filter(k -> k.getKeyID().equals(kid) && k.getAlgorithm().getName().equals(alg))
                    .findFirst()
                    .orElseThrow(() -> new CommonException(ErrorCode.NO_MATCH_APPLE_PUBLIC_KEY_ERROR));

            final RSAKey rsaKey = (RSAKey) jwk;
            if (!signedJWT.verify(new RSASSAVerifier(rsaKey.toRSAPublicKey()))) {
                throw new CommonException(ErrorCode.INVALID_APPLE_TOKEN_SIGNATURE_ERROR);
            }

            final JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            if (!issuer.equals(claims.getIssuer())) {
                throw new CommonException(ErrorCode.INVALID_APPLE_ISSUER_ERROR);
            }

            if (claims.getAudience() == null || !claims.getAudience().contains(clientId)) {
                throw new CommonException(ErrorCode.INVALID_APPLE_TOKEN_AUDIENCE_ERROR);
            }

            if (claims.getExpirationTime() == null || claims.getExpirationTime().before(new Date())) {
                throw new CommonException(ErrorCode.APPLE_IDENTITY_TOKEN_EXPIRED_ERROR);
            }

            return claims.getSubject();
        } catch (CommonException e) {
            throw e;
        } catch (ParseException | JOSEException | IOException e) {
            log.error("Apple identityToken 검증 중 오류: {}", e.getMessage(), e);
            throw new CommonException(ErrorCode.APPLE_IDENTITY_TOKEN_VERIFICATION_ERROR);
        }
    }
}
