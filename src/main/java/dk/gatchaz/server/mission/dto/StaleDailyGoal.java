package dk.gatchaz.server.mission.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 이미 지난 날짜인데 목표 라운드 수를 다 못 채운 mission_daily_goal (여행 ID + 일자).
 * 매일 자동 마감 배치(MissionDailyCloseScheduler)에서만 쓰인다.
 */
@Getter
@Setter
@NoArgsConstructor
public class StaleDailyGoal {

    private Long tripId;
    private Integer dayNo;
    private Integer targetRoundCount;
}
