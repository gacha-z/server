package dk.gatchaz.server.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 여행 팀원 일기 요약 정보 (날짜별 목록 조회 전용).
 * 본문/공개범위/AI 생성여부/상태/생성·수정 일시는 포함하지 않는다 - 목록에서는 "누가 썼는지"만 보여주고,
 * 본문 등 상세 내용은 단건 조회(GET /trips/{tripId}/diaries/{diaryId})에서 확인한다.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "여행 팀원 일기 요약 정보 (날짜별 목록 조회 전용)")
public class DiarySummaryResponse {

    @Schema(description = "일기 ID", example = "1")
    private Long diaryId;

    @Schema(description = "연결된 여행 ID", example = "1")
    private Long tripId;

    @Schema(description = "작성자 회원 ID", example = "1")
    private Long memberId;

    @Schema(description = "작성자 닉네임", example = "산민")
    private String nickname;

    @Schema(description = "일기 날짜", example = "2026-08-01")
    private LocalDate diaryDate;
}
