package dk.gatchaz.server.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 회원 정보 부분 수정 요청. 포함된(null 이 아닌) 필드만 수정하고 나머지는 기존 값을 유지한다.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "회원 정보 수정 요청. 수정할 필드만 보내면 되고, 보내지 않은 필드는 기존 값이 유지된다.")
public class MemberUpdateRequest {

    @Schema(description = "닉네임. 보내는 경우 공백일 수 없다.", example = "상민2")
    @Size(max = 50)
    private String nickname;

    @Schema(description = "나이", example = "28")
    @Min(1)
    @Max(150)
    private Integer age;
}
