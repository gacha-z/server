package dk.gatchaz.server.mission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 여행 미션 이력 1건 (하루 중 한 라운드).
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "여행 미션 이력 1건")
public class MissionHistoryItemResponse {

    @Schema(description = "몇 일차인지 (mission_start_at 기준, 시작일 = 1일차)", example = "1")
    private int dayNo;

    @Schema(description = "해당 일자 내 몇 번째 라운드인지", example = "1")
    private int assignedOrder;

    @Schema(description = "진행 미션(trip_mission) ID", example = "1")
    private Long tripMissionId;

    @Schema(description = "미션 ID", example = "10")
    private Long missionId;

    @Schema(description = "미션 유형", example = "PHOTO")
    private String missionType;

    @Schema(description = "미션 제목", example = "노을 지는 바다 사진 찍기")
    private String title;

    @Schema(description = "미션 설명")
    private String description;

    @Schema(description = "미션 난이도", example = "2")
    private Integer difficulty;

    @Schema(description = "진행 상태 (IN_PROGRESS/COMPLETED/FAILED/NOT_PERFORMED)", example = "COMPLETED")
    private String status;

    @Schema(description = "선택(시작)된 일시. NOT_PERFORMED 는 실제로 진행된 적이 없어 null")
    private LocalDateTime startedAt;

    @Schema(description = "완료된 일시. 완료되지 않았으면 null")
    private LocalDateTime completedAt;

    @Schema(description = "실패/포기된 일시. 실패/포기되지 않았으면 null")
    private LocalDateTime failedAt;
}
