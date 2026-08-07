package dk.gatchaz.server.trip.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 여행 상세 조회 응답.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "여행 상세 정보")
public class TripDetailResponse {

    @Schema(description = "여행 ID", example = "1")
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

    @Schema(description = "하루 최소 미션 수", example = "1")
    private Integer missionMin;

    @Schema(description = "하루 최대 미션 수", example = "3")
    private Integer missionMax;

    @Schema(description = "첫 미션 시작 일시 (여행 시작일 + 생성 시 설정한 시각)", example = "2026-07-10T10:00:00")
    private LocalDateTime missionStartAt;

    @Schema(description = "여행 생성자(owner) 회원 ID", example = "1")
    private Long ownerMemberId;

    @Schema(description = "확정된 여행 지역 ID. 아직 지역을 선택하지 않았으면 null", example = "5")
    private Long tripRegionId;

    @Schema(description = "확정된 여행 지역명. 아직 지역을 선택하지 않았으면 null", example = "제주")
    private String tripRegionName;

    @Schema(description = "확정된 여행 지역 이미지 URL. 아직 지역을 선택하지 않았으면 null", example = "https://images.unsplash.com/photo-jeju")
    private String tripRegionImageUrl;

    @Schema(description = "여행 생성 일시", example = "2026-07-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시. 수정 이력이 없으면 null", example = "2026-07-02T10:00:00")
    private LocalDateTime updatedAt;
}
