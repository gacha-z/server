package dk.gatchaz.server.collection.service;

import dk.gatchaz.server.collection.dto.BadgeMeta;
import dk.gatchaz.server.collection.dto.BadgeResponse;
import dk.gatchaz.server.collection.dto.CollectionItemResponse;
import dk.gatchaz.server.collection.dto.MemberBadgeProgress;
import dk.gatchaz.server.collection.dto.MemberDiaryCount;
import dk.gatchaz.server.collection.dto.RegionVisitCount;
import dk.gatchaz.server.collection.dto.TripRegionGroupInfo;
import dk.gatchaz.server.collection.mapper.CollectionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 도감(아이템/배지) 조회 + 지급 처리.
 *
 * 지급 로직은 이벤트 리스너({@link dk.gatchaz.server.collection.event.CollectionEventListener})에서만 호출된다.
 * 각 도메인 서비스(mission/diary 등)는 이벤트만 발행하고 이 모듈을 알지 못한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollectionService {

    private final CollectionMapper collectionMapper;

    @Transactional(readOnly = true)
    public List<CollectionItemResponse> getItems(final Long userId) {
        return collectionMapper.selectItems(userId);
    }

    @Transactional(readOnly = true)
    public List<BadgeResponse> getBadges(final Long userId) {
        return collectionMapper.selectBadges(userId);
    }

    /**
     * 여행에서 방문한 지역(그룹)에 해당하는 아이템을 지급한다. 이미 보유한 아이템이면 아무 일도 하지 않는다.
     */
    @Transactional
    public void awardRegionItem(final Long memberId, final Long regionGroupId, final Long tripId) {
        final Long collectionItemId = collectionMapper.selectCollectionItemIdByRegionGroup(regionGroupId);
        if (collectionItemId == null) {
            log.warn("지역 그룹에 대응하는 아이템이 없음: regionGroupId={}", regionGroupId);
            return;
        }
        if (collectionMapper.existsMemberItem(memberId, collectionItemId) > 0) {
            return;
        }
        collectionMapper.insertMemberItem(memberId, collectionItemId, tripId);
    }

    /**
     * badgeCode 배지의 진행 횟수를 1 증가시킨다. 처음 진행하는 배지면 새로 만든다.
     * 이미 달성한 배지는 횟수만 계속 올라가고(기록용) achieved_at 은 최초 달성 시점 그대로 유지한다.
     * 존재하지 않는 badgeCode 면 조용히 무시한다(운영 데이터 정합성 문제로 API 흐름을 막지 않기 위함).
     */
    @Transactional
    public void incrementBadgeProgress(final Long memberId, final String badgeCode) {
        final BadgeMeta badgeMeta = collectionMapper.selectBadgeMetaByCode(badgeCode);
        if (badgeMeta == null) {
            log.warn("존재하지 않는 배지 코드: badgeCode={}", badgeCode);
            return;
        }

        final MemberBadgeProgress progress =
                collectionMapper.selectMemberBadgeProgress(memberId, badgeMeta.getBadgeId());

        if (progress == null) {
            final int newCount = 1;
            final boolean achieved = badgeMeta.getTargetCount() != null && newCount >= badgeMeta.getTargetCount();
            collectionMapper.insertMemberBadgeProgress(
                    memberId, badgeMeta.getBadgeId(), newCount,
                    achieved ? "Y" : "N", achieved ? LocalDateTime.now() : null);
            return;
        }

        final int newCount = progress.getCurrentCount() + 1;
        final boolean alreadyAchieved = "Y".equals(progress.getAchievedYn());
        final boolean nowAchieved =
                alreadyAchieved || (badgeMeta.getTargetCount() != null && newCount >= badgeMeta.getTargetCount());
        // 이미 달성했던 배지는 최초 달성 시점(achievedAt)을 그대로 유지한다. 새로 달성하는 순간만 now() 로 기록한다.
        final LocalDateTime achievedAt = alreadyAchieved
                ? progress.getAchievedAt()
                : (nowAchieved ? LocalDateTime.now() : null);
        collectionMapper.updateMemberBadgeProgress(
                memberId, badgeMeta.getBadgeId(), newCount, nowAchieved ? "Y" : "N", achievedAt);
    }

    /**
     * 여행에 참여 중인 회원 전원의 badgeCode 진행 횟수를 1씩 증가시킨다. (팀 단위 성취를 개인 배지로 반영)
     */
    @Transactional
    public void incrementBadgeProgressForTripMembers(final Long tripId, final String badgeCode) {
        final List<Long> memberIds = collectionMapper.selectJoinedMemberIds(tripId);
        for (final Long memberId : memberIds) {
            incrementBadgeProgress(memberId, badgeCode);
        }
    }

    /**
     * badgeCode 배지의 진행 횟수를 newCount 값으로 그대로 맞춘다. (매번 다시 집계해서 반영하는 배지용 -
     * incrementBadgeProgress 와 달리 +1 이 아니라 절대값으로 덮어쓴다)
     * 이미 달성한 배지는 achieved_at 을 최초 달성 시점 그대로 유지한다.
     * 존재하지 않는 badgeCode 면 조용히 무시한다(운영 데이터 정합성 문제로 API 흐름을 막지 않기 위함).
     */
    @Transactional
    public void setBadgeProgress(final Long memberId, final String badgeCode, final int newCount) {
        final BadgeMeta badgeMeta = collectionMapper.selectBadgeMetaByCode(badgeCode);
        if (badgeMeta == null) {
            log.warn("존재하지 않는 배지 코드: badgeCode={}", badgeCode);
            return;
        }

        final MemberBadgeProgress progress =
                collectionMapper.selectMemberBadgeProgress(memberId, badgeMeta.getBadgeId());

        final boolean alreadyAchieved = progress != null && "Y".equals(progress.getAchievedYn());
        final boolean nowAchieved =
                alreadyAchieved || (badgeMeta.getTargetCount() != null && newCount >= badgeMeta.getTargetCount());
        final LocalDateTime achievedAt = alreadyAchieved
                ? progress.getAchievedAt()
                : (nowAchieved ? LocalDateTime.now() : null);

        if (progress == null) {
            collectionMapper.insertMemberBadgeProgress(
                    memberId, badgeMeta.getBadgeId(), newCount, nowAchieved ? "Y" : "N", achievedAt);
        } else {
            collectionMapper.updateMemberBadgeProgress(
                    memberId, badgeMeta.getBadgeId(), newCount, nowAchieved ? "Y" : "N", achievedAt);
        }
    }

    /**
     * 회원(memberId)의 지역 탐험 배지(서로 다른 지역 3회 방문 / 동일 지역 3회 방문) 진행도를
     * 완료된 여행 이력 전체를 다시 집계해서 반영한다. (단순 +1 이 아니라 매번 재계산 - 여행 완료마다 호출된다)
     */
    private void updateRegionExploreBadges(final Long memberId) {
        final List<RegionVisitCount> visitCounts = collectionMapper.selectRegionVisitCounts(memberId);
        final int distinctRegionCount = visitCounts.size();
        final int maxSameRegionCount = visitCounts.stream()
                .mapToInt(RegionVisitCount::getVisitCount)
                .max()
                .orElse(0);

        setBadgeProgress(memberId, "NEW_PLACE_3", distinctRegionCount);
        setBadgeProgress(memberId, "FAVORITE_PLACE_3", maxSameRegionCount);
    }

    /**
     * 여행 완료 시 지급되는 모든 도감 보상을 처리한다.
     * (여행 완료 이벤트 리스너에서만 호출된다)
     * - 여행 횟수 배지: FIRST_TRIP / TRIP_5 / TRIP_10 (참여 회원 전원, 완료할 때마다 +1)
     * - 지역별 배지: EXPLORER_&lt;지역그룹코드&gt; (참여 회원 전원, 그 지역으로 완료할 때마다 +1)
     * - 지역 탐험 배지: NEW_PLACE_3 / FAVORITE_PLACE_3 (참여 회원 전원, 완료된 여행 이력 전체를 다시 집계)
     * - 지역 아이템 지급: 방문한 지역(그룹)의 기념품 아이템 (참여 회원 전원, 이미 보유했으면 스킵)
     */
    @Transactional
    public void processTripCompletion(final Long tripId) {
        final TripRegionGroupInfo regionGroup = collectionMapper.selectRegionGroupInfoByTrip(tripId);
        final List<Long> memberIds = collectionMapper.selectJoinedMemberIds(tripId);

        for (final Long memberId : memberIds) {
            incrementBadgeProgress(memberId, "FIRST_TRIP");
            incrementBadgeProgress(memberId, "TRIP_5");
            incrementBadgeProgress(memberId, "TRIP_10");

            if (regionGroup != null && regionGroup.getRegionGroupCode() != null) {
                incrementBadgeProgress(memberId, "EXPLORER_" + regionGroup.getRegionGroupCode());
            }

            updateRegionExploreBadges(memberId);

            if (regionGroup != null && regionGroup.getRegionGroupId() != null) {
                awardRegionItem(memberId, regionGroup.getRegionGroupId(), tripId);
            }
        }
    }

    /**
     * badgeCode 배지의 진행 횟수를 amount 만큼 줄인다. 0 밑으로는 내려가지 않는다.
     * 줄인 결과가 target_count 밑으로 떨어지면 achieved_yn/achieved_at 도 미달성으로 되돌린다.
     * (다른 여행에서 얻은 진행분까지는 건드리지 않는다 - amount 만큼만 정확히 되돌린다)
     * 아직 한 번도 진행되지 않은 배지거나 존재하지 않는 badgeCode 면 조용히 무시한다.
     */
    @Transactional
    public void decrementBadgeProgress(final Long memberId, final String badgeCode, final int amount) {
        if (amount <= 0) {
            return;
        }
        final BadgeMeta badgeMeta = collectionMapper.selectBadgeMetaByCode(badgeCode);
        if (badgeMeta == null) {
            log.warn("존재하지 않는 배지 코드: badgeCode={}", badgeCode);
            return;
        }

        final MemberBadgeProgress progress =
                collectionMapper.selectMemberBadgeProgress(memberId, badgeMeta.getBadgeId());
        if (progress == null) {
            return;
        }

        final int newCount = Math.max(0, progress.getCurrentCount() - amount);
        final boolean nowAchieved = badgeMeta.getTargetCount() != null && newCount >= badgeMeta.getTargetCount();
        // 되돌린 뒤에도 여전히 달성 기준을 넘으면(다른 여행에서 얻은 진행분만으로) 기존 achieved_at 을 유지하고,
        // 기준 밑으로 떨어지면 미달성으로 되돌린다.
        final LocalDateTime achievedAt = nowAchieved ? progress.getAchievedAt() : null;
        collectionMapper.updateMemberBadgeProgress(
                memberId, badgeMeta.getBadgeId(), newCount, nowAchieved ? "Y" : "N", achievedAt);
    }

    /**
     * 여행이 취소되면 그 여행에서 오른 배지 진행도를 전부 되돌린다.
     * 여행 기록과 미션 수행 이력(trip_mission)/일기(diary) 자체는 그대로 남긴 채, 그 이력을 다시 세어
     * 정확히 그만큼만 배지 진행도에서 빼는 방식이라 다른 여행에서 얻은 진행분은 건드리지 않는다.
     * (여행 완료 때만 지급되는 여행 횟수/지역별/지역 탐험 배지·아이템은 취소로는 지급된 적이 없어 대상이 아니다)
     */
    @Transactional
    public void revertTripContributions(final Long tripId) {
        final List<Long> memberIds = collectionMapper.selectJoinedMemberIds(tripId);

        final int completedMissionCount = collectionMapper.countCompletedMissionsByTrip(tripId);
        final int foodCompletedCount = collectionMapper.countCompletedMissionsByTripAndType(tripId, "FOOD");
        final int cafeCompletedCount = collectionMapper.countCompletedMissionsByTripAndType(tripId, "CAFE");

        for (final Long memberId : memberIds) {
            decrementBadgeProgress(memberId, "FIRST_MISSION", completedMissionCount);
            decrementBadgeProgress(memberId, "MISSION_EXPERT_5", completedMissionCount);
            decrementBadgeProgress(memberId, "FIRST_FOOD", foodCompletedCount);
            decrementBadgeProgress(memberId, "FOOD_EXPERT_10", foodCompletedCount);
            decrementBadgeProgress(memberId, "FIRST_CAFE", cafeCompletedCount);
            decrementBadgeProgress(memberId, "CAFE_EXPERT_10", cafeCompletedCount);
        }

        // 일기 배지는 팀 전원이 아니라 작성자 본인만 올라가므로, 작성자별로 따로 되돌린다.
        final List<MemberDiaryCount> diaryCounts = collectionMapper.selectDiaryCountsByTrip(tripId);
        for (final MemberDiaryCount diaryCount : diaryCounts) {
            decrementBadgeProgress(diaryCount.getMemberId(), "FIRST_DIARY", diaryCount.getDiaryCount());
            decrementBadgeProgress(diaryCount.getMemberId(), "DIARY_10", diaryCount.getDiaryCount());
        }
    }
}
