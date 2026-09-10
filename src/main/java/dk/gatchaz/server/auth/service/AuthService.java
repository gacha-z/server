package dk.gatchaz.server.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import dk.gatchaz.server.auth.dto.RefreshTokenResponse;
import dk.gatchaz.server.auth.dto.RefreshTokenSaveParam;
import dk.gatchaz.server.auth.dto.SocialAccountResponse;
import dk.gatchaz.server.auth.dto.SocialAccountSaveParam;
import dk.gatchaz.server.auth.dto.SocialLoginResponse;
import dk.gatchaz.server.auth.mapper.AuthMapper;
import dk.gatchaz.server.auth.dto.JwtTokenDto;
import dk.gatchaz.server.common.exception.CommonException;
import dk.gatchaz.server.common.exception.ErrorCode;
import dk.gatchaz.server.member.dto.MemberCreateParam;
import dk.gatchaz.server.member.mapper.MemberMapper;
import dk.gatchaz.server.common.security.apple.AppleTokenVerifier;
import dk.gatchaz.server.common.security.google.GoogleTokenVerifier;
import dk.gatchaz.server.type.EProvider;
import dk.gatchaz.server.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthMapper authMapper;
    private final MemberMapper memberMapper;
    private final JwtUtil jwtUtil;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final AppleTokenVerifier appleTokenVerifier;

    /**
     * 구글 로그인. id_token 을 검증하고, 이미 가입된 소셜 계정이면 로그인, 아니면 회원을 새로 만든다(자동 가입).
     */
    @Transactional
    public SocialLoginResponse loginWithGoogle(final String idToken) {
        final GoogleIdToken.Payload payload = googleTokenVerifier.verifyAndGetPayload(idToken);
        return login(EProvider.GOOGLE.name(), payload.getSubject(), payload.getEmail());
    }

    /**
     * 애플 로그인. identityToken 을 검증하고, 이미 가입된 소셜 계정이면 로그인, 아니면 회원을 새로 만든다(자동 가입).
     * (애플은 최초 로그인 응답에만 이메일을 내려주고 이후에는 안 내려주는 경우가 많아, 이메일은 저장하지 않는다.)
     */
    @Transactional
    public SocialLoginResponse loginWithApple(final String identityToken) {
        final String providerUserId = appleTokenVerifier.verifyAndGetProviderUserId(identityToken);
        return login(EProvider.APPLE.name(), providerUserId, null);
    }

    private SocialLoginResponse login(final String provider, final String providerUserId, final String email) {
        final SocialAccountResponse existing = authMapper.selectSocialAccount(provider, providerUserId);

        final Long memberId;
        final boolean newMember;
        if (existing != null) {
            memberId = existing.getMemberId();
            newMember = false;
        } else {
            memberId = provisionNewMember(provider, providerUserId, email);
            newMember = true;
        }

        final JwtTokenDto tokens = jwtUtil.generateTokens(memberId);
        saveRefreshToken(memberId, tokens.getRefreshToken());

        return SocialLoginResponse.builder()
                .memberId(memberId)
                .accessToken(tokens.getAccessToken())
                .refreshToken(tokens.getRefreshToken())
                .newMember(newMember)
                .build();
    }

    /**
     * 최초 소셜 로그인 - 닉네임/나이가 없는 빈 회원(member)을 만들고 소셜 계정을 연결한다.
     * TODO: 닉네임/나이는 이후 MemberController(PATCH /api/v1/members/me) 온보딩 화면에서 채운다.
     */
    private Long provisionNewMember(final String provider, final String providerUserId, final String email) {
        final MemberCreateParam memberParam = MemberCreateParam.builder().build();
        memberMapper.insertMember(memberParam);
        final Long memberId = memberParam.getMemberId();

        final SocialAccountSaveParam socialAccountParam = SocialAccountSaveParam.builder()
                .memberId(memberId)
                .provider(provider)
                .providerUserId(providerUserId)
                .email(email)
                .build();
        authMapper.insertSocialAccount(socialAccountParam);

        return memberId;
    }

    /**
     * 리프레시 토큰으로 새 액세스/리프레시 토큰 쌍을 발급한다(로테이션). 기존 리프레시 토큰은 폐기 처리한다.
     * 폐기됐거나 만료됐거나 존재하지 않는 토큰이면 예외를 던진다.
     */
    @Transactional
    public JwtTokenDto refresh(final String refreshToken) {
        final RefreshTokenResponse valid = authMapper.selectValidRefreshToken(refreshToken);
        if (valid == null) {
            throw new CommonException(ErrorCode.INVALID_TOKEN_ERROR);
        }

        authMapper.revokeRefreshToken(refreshToken);

        final JwtTokenDto tokens = jwtUtil.generateTokens(valid.getMemberId());
        saveRefreshToken(valid.getMemberId(), tokens.getRefreshToken());

        return tokens;
    }

    /**
     * 로그아웃. 회원(memberId)의 유효한 리프레시 토큰을 모두 폐기한다(모든 기기 로그아웃).
     */
    @Transactional
    public void logout(final Long memberId) {
        authMapper.revokeAllRefreshTokensByMember(memberId);
    }

    private void saveRefreshToken(final Long memberId, final String refreshToken) {
        final RefreshTokenSaveParam param = RefreshTokenSaveParam.builder()
                .memberId(memberId)
                .token(refreshToken)
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(jwtUtil.getRefreshExpirationMs())))
                .build();
        authMapper.insertRefreshToken(param);
    }
}
