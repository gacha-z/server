package dk.gatchaz.server.notification.event;

import dk.gatchaz.server.notification.service.NotificationService;
import dk.gatchaz.server.type.EDiaryVisibility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 어떤 사건에 알림을 보낼지 결정하는 지점.
 * 각 도메인 서비스는 이벤트만 발행하고 알림 모듈을 알지 못한다.
 *
 * 미션 기능이 개발되면 미션 시작/다음 미션 이벤트에 대한 처리도 여기에 추가한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    /**
     * 팀원이 일기를 작성하면 같은 여행의 다른 참여자에게 알린다.
     *
     * 일기 저장이 커밋된 뒤(AFTER_COMMIT) 별도 스레드에서 처리한다.
     * - 저장이 롤백되면 알림이 나가지 않는다.
     * - 푸시 발송 시간이 일기 작성 API 응답을 늦추지 않는다.
     * 알림 실패는 여기서 로그로만 남긴다. (이미 커밋된 일기에는 영향을 주지 않는다)
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDiaryCreated(final DiaryCreatedEvent event) {
        // 본인만 보는 일기(PRIVATE)는 팀원에게 알리지 않는다.
        if (EDiaryVisibility.PRIVATE == event.visibility()) {
            return;
        }
        try {
            notificationService.notifyDiaryCreated(event.diaryId(), event.tripId(), event.writerMemberId());
        } catch (final Exception e) {
            log.error("일기 작성 알림 처리 실패: diaryId={}, tripId={}, message={}",
                    event.diaryId(), event.tripId(), e.getMessage(), e);
        }
    }
}
