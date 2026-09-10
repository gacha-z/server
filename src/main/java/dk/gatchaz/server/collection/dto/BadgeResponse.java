package dk.gatchaz.server.collection.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 배지 1개 (전체 목록 + 회원 기준 진행률/달성 여부).
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "배지")
public class BadgeResponse {

    @Schema(description = "배지 ID", example = "1")
    private Long badgeId;

    @Schema(description = "배지 분류", example = "TRIP_COUNT")
    private String badgeGroup;

    @Schema(description = "배지 코드", example = "FIRST_TRIP")
    private String badgeCode;

    @Schema(description = "배지 이름", example = "첫 여행 완료")
    private String badgeName;

    @Schema(description = "배지 설명")
    private String description;

    @Schema(description = "달성 기준 횟수", example = "1")
    private Integer targetCount;

    @Schema(description = "현재 진행 횟수 (미보유면 0)", example = "0")
    private Integer currentCount;

    @Schema(description = "달성 여부 (Y/N)", example = "N")
    private String achievedYn;

    @Schema(description = "달성 일시. 미달성이면 null")
    private LocalDateTime achievedAt;
}
