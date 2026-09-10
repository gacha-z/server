package dk.gatchaz.server.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "토큰 재발급 요청")
public class RefreshRequest {

    @Schema(description = "기존에 발급받은 리프레시 토큰")
    @NotBlank
    private String refreshToken;
}
