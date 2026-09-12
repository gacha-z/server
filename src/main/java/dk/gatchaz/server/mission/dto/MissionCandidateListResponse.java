package dk.gatchaz.server.mission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 미션 후보 조회 응답.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "미션 후보 조회 응답")
public class MissionCandidateListResponse {

    @Schema(description = "몇 일차인지 (mission_start_at 기준, 시작일 = 1일차)", example = "1")
    private int dayNo;

    @Schema(description = "해당 일자 내 몇 번째 라운드인지", example = "1")
    private int assignedOrder;

    @Schema(description = "그 날의 목표 미션 라운드 수 (mission_min~mission_max 사이에서 정해져 하루 동안 고정)", example = "3")
    private int targetRoundCount;

    @Schema(description = "이번 라운드에서 미션을 선택/리롤할 수 있는 담당자 회원 ID (참여자 중 무작위 배정)", example = "3")
    private Long pickerMemberId;

    @Schema(description = "미션 후보 3개")
    private List<MissionCandidateResponse> candidates;
}
