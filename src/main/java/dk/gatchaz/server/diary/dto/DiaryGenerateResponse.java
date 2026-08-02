package dk.gatchaz.server.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI 일기 초안 생성 응답 (저장되지 않음)")
public class DiaryGenerateResponse {

    /** AI가 생성한 일기 본문 초안 (저장 전 상태) */
    @Schema(description = "AI가 생성한 일기 본문 초안. 사용자가 검토·수정 후 저장 API(POST /diaries)로 저장한다.", example = "강릉에 도착해 안목해변에 갔다. 노을이 정말 예뻤다. 친구와 오랜만에 수다를 떨며 커피를 마셨다.")
    private String content;

    /** AI 생성 여부(항상 true). 저장 API(POST /diaries)에 isAiGenerated=true 로 넘기면 is_ai_generated='Y' 로 기록된다. */
    @Schema(description = "AI 생성 여부(항상 true)", example = "true")
    private boolean aiGenerated;
}
