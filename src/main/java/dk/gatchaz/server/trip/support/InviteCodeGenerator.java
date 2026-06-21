package dk.gatchaz.server.trip.support;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * 여행 초대 코드 생성기.
 * base62(0-9, A-Z, a-z) 문자 중 SecureRandom 으로 8자를 무작위로 뽑아 추측 불가능한 코드를 만든다.
 * (경우의 수 62^8 ≈ 2.2 * 10^14 이라 충돌은 사실상 발생하지 않으며, 충돌 시에는 호출부에서 재생성한다.)
 */
@Component
public class InviteCodeGenerator {

    private static final char[] CHARSET =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final int CODE_LENGTH = 8;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        final StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARSET[secureRandom.nextInt(CHARSET.length)]);
        }
        return sb.toString();
    }
}
