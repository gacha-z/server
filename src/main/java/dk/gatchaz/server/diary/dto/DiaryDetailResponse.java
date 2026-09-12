package dk.gatchaz.server.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

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

    @Schema(description = "작성자 닉네임", example = "산민")
    private String nickname;

    @Schema(description = "일기 본문", example = "오늘은 강릉 안목해변에서 커피를 마셨다.")
    private String content;

    @Schema(description = "일기 날짜", example = "2026-08-01")
    private LocalDate diaryDate;
}
