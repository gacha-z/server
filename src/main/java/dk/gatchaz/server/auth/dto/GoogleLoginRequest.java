package dk.gatchaz.server.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "구글 로그인 요청")
public class GoogleLoginRequest {

    @Schema(description = "앱에서 Google Sign-In SDK로 발급받은 id_token")
    @NotBlank
    private String idToken;
}
