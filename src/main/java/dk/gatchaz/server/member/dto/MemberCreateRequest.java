package dk.gatchaz.server.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "회원가입 기본 정보 저장 요청")
public class MemberCreateRequest {

    /** 닉네임 (필수) */
    @Schema(description = "닉네임", example = "상민")
    @NotBlank
    @Size(max = 50)
    private String nickname;

    /** 나이 (필수) */
    @Schema(description = "나이", example = "27")
    @NotNull
    @Min(1)
    @Max(150)
    private Integer age;
}
