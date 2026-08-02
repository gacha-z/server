package dk.gatchaz.server.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "AI 일기 초안 생성 요청")
public class DiaryGenerateRequest {

    /** 연결할 여행 ID(선택). 지정 시 그 여행의 지역/기간을 반영해 더 구체적으로 생성한다. */
    @Schema(description = "여행 ID(선택). 지정 시 여행 지역/기간을 프롬프트에 반영한다.", example = "1")
    private Long tripId;

    /** 요청한 회원 ID (필수). 로그인 연동 전까지 요청으로 받는다. */
    @Schema(description = "요청한 회원 ID (필수). 로그인 연동 전까지 요청으로 받는다.", example = "1")
    @NotNull
    private Long memberId;

    /** 사용자가 입력한 일기 내용 (필수). 이 내용을 바탕으로 AI가 일기를 작성한다. */
    @Schema(description = "사용자가 입력한 일기 내용 (필수)", example = "안목해변에서 커피 마시고 노을 봄. 친구랑 오랜만에 수다 떨어서 좋았음")
    @NotBlank
    private String content;

    /** 시스템 프롬프트 override(선택, 내부·테스트용). 주면 이 값으로 서버 고정 프롬프트를 덮어쓴다. Swagger 문서에는 노출하지 않는다(@Schema hidden). */
    @Schema(hidden = true)
    private String promptOverride;
}
