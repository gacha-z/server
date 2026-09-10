package dk.gatchaz.server.common.security.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import dk.gatchaz.server.common.exception.CommonException;
import dk.gatchaz.server.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Google Sign-In 으로 앱(Android/iOS)이 발급받은 id_token 을 서버가 직접 검증한다.
 * Spring 의 oauth2Login(인가 코드 교환) 방식은 쓰지 않는 앱 전용 로그인이므로, client-id 로
 * 서명/발급자/만료/audience 만 검증한다 (GoogleIdTokenVerifier 가 내부적으로 전부 처리해준다).
 */
@Component
@Slf4j
public class GoogleTokenVerifier {

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifier(@Value("${oauth2.google.client-id}") String clientId) {
        // 여러 클라이언트(Android/iOS/웹)를 등록했다면 콤마(,)로 구분해서 넣는다.
        final List<String> audiences = Arrays.stream(clientId.split(","))
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .toList();

        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(audiences)
                .build();
    }

    public GoogleIdToken.Payload verifyAndGetPayload(String idTokenString) {
        try {
            final GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                log.warn("Google id_token 검증 실패: 서명/발급자/만료/audience 중 하나가 유효하지 않음");
                throw new CommonException(ErrorCode.INVALID_GOOGLE_TOKEN);
            }
            return idToken.getPayload();
        } catch (CommonException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google id_token 검증 중 오류: {}", e.getMessage(), e);
            throw new CommonException(ErrorCode.INVALID_GOOGLE_TOKEN);
        }
    }
}
