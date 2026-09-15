package dk.gatchaz.server.mission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 여행 미션 이력 - 하루(dayNo) 단위로 묶은 라운드 목록.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "일자별 미션 이력")
public class MissionHistoryDayResponse {

    @Schema(description = "몇 일차인지 (mission_start_at 기준, 시작일 = 1일차)", example = "1")
    private int dayNo;

    @Schema(description = "그 날 진행된 라운드 목록 (assignedOrder 순)")
    private List<MissionHistoryItemResponse> missions;
}
