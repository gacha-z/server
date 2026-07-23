package dk.gatchaz.server.trip.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 여행 목록 조회 시 각 여행의 요약 정보.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "여행 요약 정보")
public class TripSummaryResponse {

    @Schema(description = "여행 ID", example = "42")
    private Long tripId;

    @Schema(description = "여행 이름", example = "제주 여름 여행")
    private String title;

    @Schema(description = "여행 시작일", example = "2026-07-10")
    private LocalDate startDate;

    @Schema(description = "여행 종료일", example = "2026-07-12")
    private LocalDate endDate;

    @Schema(description = "여행 상태 (CREATED: 생성됨 / CANCELLED: 취소됨 / COMPLETED: 완료됨)", example = "CREATED")
    private String status;

    @Schema(description = "여행 정원", example = "6")
    private Integer memberLimit;

    @Schema(description = "현재 참여(JOINED) 인원 수", example = "3")
    private Integer joinedMemberCount;

    @Schema(description = "확정된 여행 지역 ID. 아직 지역을 선택하지 않았으면 null", example = "5")
    private Long tripRegionId;

    @Schema(description = "확정된 여행 지역명. 아직 지역을 선택하지 않았으면 null", example = "제주")
    private String tripRegionName;

    @Schema(description = "여행 생성 일시", example = "2026-07-01T10:00:00")
    private LocalDateTime createdAt;
}
