package dk.gatchaz.server.auth.controller;

import dk.gatchaz.server.common.annotation.UserId;
import dk.gatchaz.server.auth.dto.AppleLoginRequest;
import dk.gatchaz.server.auth.dto.GoogleLoginRequest;
import dk.gatchaz.server.auth.dto.RefreshRequest;
import dk.gatchaz.server.auth.dto.SocialLoginResponse;
import dk.gatchaz.server.auth.service.AuthService;
import dk.gatchaz.server.auth.dto.JwtTokenDto;
import dk.gatchaz.server.common.dto.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "로그인/인증 API (앱 전용 - Google/Apple id_token 서버 직접 검증)")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "구글 로그인",
            description = "앱에서 Google Sign-In SDK 로 발급받은 id_token 을 검증하고 로그인한다. " +
                    "처음 로그인하는 계정이면 회원을 자동 생성하며, 응답의 newMember 가 true 이면 온보딩(닉네임/나이 입력)이 필요하다.")
    @PostMapping("/login/google")
    public ResponseDto<SocialLoginResponse> loginWithGoogle(@Valid @RequestBody final GoogleLoginRequest request) {
        return ResponseDto.ok(authService.loginWithGoogle(request.getIdToken()));
    }

    @Operation(
            summary = "애플 로그인",
            description = "앱에서 Sign in with Apple 로 발급받은 identityToken 을 검증하고 로그인한다. " +
                    "처음 로그인하는 계정이면 회원을 자동 생성하며, 응답의 newMember 가 true 이면 온보딩(닉네임/나이 입력)이 필요하다.")
    @PostMapping("/login/apple")
    public ResponseDto<SocialLoginResponse> loginWithApple(@Valid @RequestBody final AppleLoginRequest request) {
        return ResponseDto.ok(authService.loginWithApple(request.getIdentityToken()));
    }

    @Operation(
            summary = "토큰 재발급",
            description = "유효한 리프레시 토큰으로 새 액세스/리프레시 토큰 쌍을 발급받는다. (로테이션: 기존 리프레시 토큰은 폐기된다)")
    @PostMapping("/refresh")
    public ResponseDto<JwtTokenDto> refresh(@Valid @RequestBody final RefreshRequest request) {
        return ResponseDto.ok(authService.refresh(request.getRefreshToken()));
    }

    @Operation(summary = "로그아웃", description = "현재 로그인된 회원의 모든 리프레시 토큰을 폐기한다. (인증 필요)")
    @PostMapping("/logout")
    public ResponseDto<Void> logout(@UserId final Long memberId) {
        authService.logout(memberId);
        return ResponseDto.<Void>ok(null);
    }
}
