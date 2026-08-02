package dk.gatchaz.server.diary.dto;

import dk.gatchaz.server.type.EDiaryVisibility;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "일기 수정 요청 (전달한 값으로 교체)")
public class DiaryUpdateRequest {

    /** 작성자 회원 ID(선택). 로그인 연동 후 본인 확인용으로 사용 예정. */
    @Schema(description = "작성자 회원 ID(선택). 로그인 연동 후 본인 확인에 사용 예정.", example = "1")
    private Long memberId;

    /** 일기 본문 */
    @Schema(description = "일기 본문", example = "다시 정리한 오늘의 기록...")
    private String content;

    /** 공개 범위(선택). 미지정 시 TEAM. */
    @Schema(description = "공개 범위(선택, 미지정 시 TEAM). PRIVATE / TEAM / PUBLIC", example = "TEAM")
    private EDiaryVisibility visibility;
}
