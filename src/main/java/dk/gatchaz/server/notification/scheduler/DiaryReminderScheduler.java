package dk.gatchaz.server.notification.scheduler;

import dk.gatchaz.server.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 일기 작성 독려 알림 배치.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DiaryReminderScheduler {

    private final NotificationService notificationService;

    /**
     * 여행 기간 중이면서 오늘 일기를 아직 쓰지 않은 참여자에게 작성을 권유한다.
     * 실행 주기는 yml(batch.diary-reminder-cron)에서 관리하며, 값을 "-" 로 두면 배치가 실행되지 않는다.
     */
    @Scheduled(cron = "${batch.diary-reminder-cron}", zone = "Asia/Seoul")
    public void notifyDiaryWriteReminder() {
        log.info("일기 작성 독려 알림 배치 시작");
        try {
            final int sentCount = notificationService.notifyDiaryWriteReminder();
            log.info("일기 작성 독려 알림 배치 종료: 대상={}명", sentCount);
        } catch (final Exception e) {
            log.error("일기 작성 독려 알림 배치 실패: {}", e.getMessage(), e);
        }
    }
}
