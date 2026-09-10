package dk.gatchaz.server.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "애플 로그인 요청")
public class AppleLoginRequest {

    @Schema(description = "앱에서 Sign in with Apple로 발급받은 identityToken")
    @NotBlank
    private String identityToken;
}
