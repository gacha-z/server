package dk.gatchaz.server.mission.scheduler;

import dk.gatchaz.server.mission.service.MissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 지난 날짜에 다 못 끝낸 미션 라운드를 매일 자동으로 마감하는 배치.
 * (진행 중이던 미션은 포기(FAILED) 처리, 아직 안 뽑은 나머지 라운드는 목표 라운드 수를 낮추지 않고
 * "미션 수행 안함"(NOT_PERFORMED) 기록을 실제로 남겨서 마감)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MissionDailyCloseScheduler {

    private final MissionService missionService;

    /**
     * 매일 새벽 4시에 실행한다. 실행 주기는 yml(batch.mission-daily-close-cron)에서 관리하며,
     * 값을 "-" 로 두면 배치가 실행되지 않는다.
     */
    @Scheduled(cron = "${batch.mission-daily-close-cron}", zone = "Asia/Seoul")
    public void closeStaleDailyGoals() {
        log.info("지난 날짜 마감 배치 시작");
        try {
            missionService.closeStaleDailyGoals();
        } catch (final Exception e) {
            log.error("지난 날짜 마감 배치 실패: {}", e.getMessage(), e);
        }
        log.info("지난 날짜 마감 배치 종료");
    }
}
