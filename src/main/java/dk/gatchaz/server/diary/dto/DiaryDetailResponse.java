package dk.gatchaz.server.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 일기 정보 (상세 조회 및 목록 공용).
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "일기 정보")
public class DiaryDetailResponse {

    @Schema(description = "일기 ID", example = "1")
    private Long diaryId;

    @Schema(description = "연결된 여행 ID", example = "1")
    private Long tripId;

    @Schema(description = "작성자 회원 ID", example = "1")
    private Long memberId;

    @Schema(description = "일기 본문", example = "오늘은 강릉 안목해변에서 커피를 마셨다.")
    private String content;

    @Schema(description = "일기 날짜", example = "2026-08-01")
    private LocalDate diaryDate;

    @Schema(description = "공개 범위 (PRIVATE / TEAM / PUBLIC)", example = "TEAM")
    private String visibility;

    @Schema(description = "일기 상태 (ACTIVE / DELETED)", example = "ACTIVE")
    private String status;

    @Schema(description = "AI 생성 여부 (Y/N)", example = "N")
    private String isAiGenerated;

    @Schema(description = "생성 일시", example = "2026-08-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시. 수정 이력이 없으면 null", example = "2026-08-01T12:00:00")
    private LocalDateTime updatedAt;
}
