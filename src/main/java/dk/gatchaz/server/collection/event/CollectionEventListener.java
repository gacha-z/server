package dk.gatchaz.server.collection.event;

import dk.gatchaz.server.collection.service.CollectionService;
import dk.gatchaz.server.notification.event.DiaryCreatedEvent;
import dk.gatchaz.server.notification.event.MissionCompletedEvent;
import dk.gatchaz.server.notification.event.TripCancelledEvent;
import dk.gatchaz.server.notification.event.TripCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 어떤 사건에 배지/아이템을 지급할지 결정하는 지점.
 * 각 도메인 서비스는 이벤트만 발행하고 도감 모듈을 알지 못한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CollectionEventListener {

    private final CollectionService collectionService;

    /**
     * 일기를 작성하면 일기 관련 배지 진행도를 올린다.
     * 일기 저장이 커밋된 뒤(AFTER_COMMIT) 별도 스레드에서 처리한다.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDiaryCreated(final DiaryCreatedEvent event) {
        try {
            collectionService.incrementBadgeProgress(event.writerMemberId(), "FIRST_DIARY");
            collectionService.incrementBadgeProgress(event.writerMemberId(), "DIARY_10");
        } catch (final Exception e) {
            log.error("일기 배지 처리 실패: diaryId={}, memberId={}, message={}",
                    event.diaryId(), event.writerMemberId(), e.getMessage(), e);
        }
    }

    /**
     * 미션이 완료되면 여행에 참여 중인 회원 전원의 미션 성공 배지 진행도를 올리고,
     * FOOD/CAFE 미션이면 해당 배지 진행도도 함께 올린다.
     * 완료 처리가 커밋된 뒤(AFTER_COMMIT) 별도 스레드에서 처리한다.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMissionCompleted(final MissionCompletedEvent event) {
        try {
            collectionService.incrementBadgeProgressForTripMembers(event.tripId(), "FIRST_MISSION");
            collectionService.incrementBadgeProgressForTripMembers(event.tripId(), "MISSION_EXPERT_5");

            if ("FOOD".equals(event.missionType())) {
                collectionService.incrementBadgeProgressForTripMembers(event.tripId(), "FIRST_FOOD");
                collectionService.incrementBadgeProgressForTripMembers(event.tripId(), "FOOD_EXPERT_10");
            } else if ("CAFE".equals(event.missionType())) {
                collectionService.incrementBadgeProgressForTripMembers(event.tripId(), "FIRST_CAFE");
                collectionService.incrementBadgeProgressForTripMembers(event.tripId(), "CAFE_EXPERT_10");
            }
        } catch (final Exception e) {
            log.error("미션 완료 배지 처리 실패: tripId={}, tripMissionId={}, message={}",
                    event.tripId(), event.tripMissionId(), e.getMessage(), e);
        }
    }

    /**
     * 여행이 완료되면 참여 회원 전원에게 여행 횟수 배지, 지역별 배지, 지역 탐험 배지 진행도를 반영하고
     * 방문 지역 아이템을 지급한다.
     * 완료 처리가 커밋된 뒤(AFTER_COMMIT) 별도 스레드에서 처리한다.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTripCompleted(final TripCompletedEvent event) {
        try {
            collectionService.processTripCompletion(event.tripId());
        } catch (final Exception e) {
            log.error("여행 완료 도감 처리 실패: tripId={}, message={}", event.tripId(), e.getMessage(), e);
        }
    }

    /**
     * 여행이 취소되면 그 여행에서 오른 배지 진행도(미션 성공/FOOD/CAFE/일기)를 되돌린다.
     * 여행 기록과 미션 수행 이력은 그대로 남기고 배지 진행도만 되돌린다.
     * 취소 처리가 커밋된 뒤(AFTER_COMMIT) 별도 스레드에서 처리한다.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTripCancelled(final TripCancelledEvent event) {
        try {
            collectionService.revertTripContributions(event.tripId());
        } catch (final Exception e) {
            log.error("여행 취소 배지 되돌리기 실패: tripId={}, message={}", event.tripId(), e.getMessage(), e);
        }
    }
}
