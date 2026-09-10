package dk.gatchaz.server.mission.service;

import dk.gatchaz.server.common.exception.CommonException;
import dk.gatchaz.server.common.exception.ErrorCode;
import dk.gatchaz.server.mission.dto.MissionCandidateListResponse;
import dk.gatchaz.server.mission.dto.MissionCandidateRerollInfo;
import dk.gatchaz.server.mission.dto.MissionCandidateResponse;
import dk.gatchaz.server.mission.dto.MissionCandidateSelectionInfo;
import dk.gatchaz.server.mission.dto.MissionCompleteRequest;
import dk.gatchaz.server.mission.dto.MissionInfo;
import dk.gatchaz.server.mission.dto.MissionRerollInsertParam;
import dk.gatchaz.server.mission.dto.MissionSelectParam;
import dk.gatchaz.server.mission.dto.MissionSelectResponse;
import dk.gatchaz.server.mission.dto.StaleDailyGoal;
import dk.gatchaz.server.mission.dto.TripMissionSettingInfo;
import dk.gatchaz.server.mission.dto.TripRegionCoordinate;
import dk.gatchaz.server.mission.mapper.MissionMapper;
import dk.gatchaz.server.mission.support.DistanceCalculator;
import dk.gatchaz.server.notification.event.MissionCompletedEvent;
import dk.gatchaz.server.notification.event.TripCompletedEvent;
import dk.gatchaz.server.type.EMissionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class MissionService {

    /** 라운드(미션 1회)당 생성되는 후보 개수 */
    private static final int CANDIDATE_COUNT = 3;

    /** 미션 완료 시 위치 인증 허용 거리(미터). 여행 지역 중심 좌표 기준. */
    private static final double LOCATION_VERIFICATION_RADIUS_METER = 10_000.0;

    private final MissionMapper missionMapper;
    private final LocationVerificationRecorder locationVerificationRecorder;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 현재 라운드의 미션 후보 3개를 조회한다. 아직 생성되지 않은 라운드면 새로 생성해서 반환한다.
     * 1. 여행 존재 여부, 지역 선택 여부, 미션 시작 여부(mission_start_at 도달)를 확인한다.
     * 2. mission_start_at 날짜 기준으로 오늘이 몇 일차(dayNo)인지 계산한다. (시작일 = 1일차)
     * 3. 그 날의 목표 라운드 수(target_round_count)가 없으면 mission_min~mission_max 사이 무작위 값으로
     *    생성해 저장한다. (하루 동안 고정)
     * 4. 완료/실패로 끝난 라운드 수(resolvedRounds)로 현재 라운드 번호를 정한다. (resolvedRounds + 1)
     *    이미 목표 라운드 수만큼 다 끝났으면 오늘은 더 이상 라운드가 없다.
     * 5. 현재 라운드의 활성 후보(rerolled_yn = 'N')를 조회한다.
     *    - 있으면 그대로 반환한다. (선택 대기 중이거나 이미 선택되어 진행 중인 라운드를 재조회하는 경우)
     *    - 없으면(아직 후보가 생성되지 않은 라운드) 이 여행에서 아직 선택(selected_yn='Y')된 적 없는 미션 중
     *      여행 지역과 일치하는 미션 3개를 무작위로 뽑아 후보로 저장하고 반환한다.
     */
    @Transactional
    public MissionCandidateListResponse getMissionCandidates(final Long tripId) {
        // 1. 여행 존재 + 지역 선택 + 미션 시작 여부 확인
        final TripMissionSettingInfo trip = missionMapper.selectTripMissionSettingInfo(tripId);
        if (trip == null) {
            throw new CommonException(ErrorCode.NOT_FOUND_TRIP);
        }
        if (trip.getTripRegionId() == null) {
            throw new CommonException(ErrorCode.TRIP_REGION_NOT_SELECTED);
        }
        if (trip.getMissionStartAt() == null || LocalDateTime.now().isBefore(trip.getMissionStartAt())) {
            throw new CommonException(ErrorCode.MISSION_NOT_STARTED);
        }

        // 2. mission_start_at 날짜 기준 며칠차인지 계산 (시작일 = 1일차)
        final int dayNo = (int) ChronoUnit.DAYS.between(trip.getMissionStartAt().toLocalDate(), LocalDate.now()) + 1;

        // 3. 오늘의 목표 라운드 수 확보 (없으면 min~max 사이 무작위로 생성, 하루 동안 고정)
        Integer targetRoundCount = missionMapper.selectDailyGoal(tripId, dayNo);
        if (targetRoundCount == null) {
            targetRoundCount = ThreadLocalRandom.current().nextInt(trip.getMissionMin(), trip.getMissionMax() + 1);
            missionMapper.insertDailyGoal(tripId, dayNo, targetRoundCount);
        }

        // 4. 완료/실패로 끝난 라운드 수로 현재 라운드 번호 계산. 목표를 다 채웠으면 오늘은 더 없음.
        final int resolvedRounds = missionMapper.countResolvedRounds(tripId, dayNo);
        if (resolvedRounds >= targetRoundCount) {
            throw new CommonException(ErrorCode.DAILY_MISSION_QUOTA_COMPLETED);
        }
        final int assignedOrder = resolvedRounds + 1;

        List<MissionCandidateResponse> candidates =
                missionMapper.selectActiveCandidates(tripId, dayNo, assignedOrder);

        // 아직 생성되지 않은 라운드면 새로 후보를 뽑아 저장한다.
        if (candidates.isEmpty()) {
            final List<Long> missionIds =
                    missionMapper.selectRandomMissionIds(CANDIDATE_COUNT, trip.getTripRegionId(), tripId);
            if (missionIds.size() < CANDIDATE_COUNT) {
                throw new CommonException(ErrorCode.NO_AVAILABLE_MISSION);
            }
            missionMapper.insertCandidates(tripId, dayNo, assignedOrder, missionIds);
            candidates = missionMapper.selectActiveCandidates(tripId, dayNo, assignedOrder);
        }

        return new MissionCandidateListResponse(dayNo, assignedOrder, targetRoundCount, candidates);
    }

    /**
     * 미션 후보(missionCandidateId)를 선택해 진행 중인 미션(trip_mission)으로 확정한다.
     * 1. 후보가 해당 여행의 활성(리롤되지 않은) + 미선택 상태인지 확인한다.
     * 2. 후보를 선택 확정한다. (selected_yn 'N' -> 'Y', 동시 선택 경합 시 실패)
     * 3. 선택된 미션을 진행 중(trip_mission, status = 'IN_PROGRESS')으로 새로 생성한다.
     */
    @Transactional
    public MissionSelectResponse selectMission(final Long tripId, final Long missionCandidateId) {
        // 1. 후보 확인
        final MissionCandidateSelectionInfo candidate =
                missionMapper.selectCandidateForSelection(tripId, missionCandidateId);
        if (candidate == null) {
            throw new CommonException(ErrorCode.INVALID_MISSION_CANDIDATE_SELECTION);
        }

        // 2. 선택 확정 (동시에 다른 요청이 먼저 선택했으면 실패)
        final int selected = missionMapper.markCandidateSelected(tripId, missionCandidateId);
        if (selected == 0) {
            throw new CommonException(ErrorCode.INVALID_MISSION_CANDIDATE_SELECTION);
        }

        // 3. 진행 중인 미션으로 생성
        final MissionSelectParam param = MissionSelectParam.builder()
                .tripId(tripId)
                .missionId(candidate.getMissionId())
                .dayNo(candidate.getDayNo())
                .assignedOrder(candidate.getAssignedOrder())
                .status(EMissionStatus.IN_PROGRESS.name())
                .build();
        missionMapper.insertTripMission(param);

        return new MissionSelectResponse(
                param.getTripMissionId(),
                candidate.getMissionId(),
                candidate.getMissionType(),
                candidate.getTitle(),
                candidate.getDescription(),
                candidate.getDifficulty(),
                LocalDateTime.now());
    }

    /**
     * 진행 중인 미션(tripMissionId)을 완료 처리한다. 하나의 트랜잭션으로 처리된다.
     * 1. 진행 중(IN_PROGRESS) 상태인지 확인한다.
     * 2. 현재 참여 중인 모든 팀원이 각자 셋로그를 촬영했는지 확인한다. (전원 촬영 필수)
     * 3. 완료 시점 좌표와 여행 지역 중심 좌표 사이 거리를 계산해 위치 인증한다. (허용 거리 10km)
     *    인증에 성공하면 이력을 기록하고 완료 처리한다. 실패하면 예외를 던지고 진행 중 상태를 유지한다(재시도 가능).
     */
    @Transactional
    public void completeMission(final Long tripId, final Long tripMissionId, final MissionCompleteRequest request) {
        // 1. 진행 중인 미션인지 확인
        if (missionMapper.existsInProgressTripMission(tripId, tripMissionId) == 0) {
            throw new CommonException(ErrorCode.NOT_FOUND_TRIP_MISSION);
        }

        // 2. 셋로그 완료 확인 (참여 중인 모든 팀원이 각자 최소 1개씩 촬영해야 함)
        final int joinedMemberCount = missionMapper.countJoinedMembers(tripId);
        final int setlogMemberCount = missionMapper.countSetlogMembers(tripMissionId);
        if (setlogMemberCount < joinedMemberCount) {
            throw new CommonException(ErrorCode.SETLOG_NOT_COMPLETE);
        }

        // 3. 위치 인증 (여행 지역 중심 좌표와의 거리, 허용 거리 이내인지)
        // 인증 이력은 완료 처리 트랜잭션과 별개로(REQUIRES_NEW) 즉시 커밋하여, 이후 실패로 예외가 던져져도 남는다.
        final TripRegionCoordinate region = missionMapper.selectTripRegionCoordinate(tripId);
        if (region == null || region.getLatitude() == null || region.getLongitude() == null) {
            locationVerificationRecorder.record(
                    tripId, tripMissionId, request.getMemberId(),
                    request.getLatitude(), request.getLongitude(), null, false);
            throw new CommonException(ErrorCode.LOCATION_VERIFICATION_FAILED);
        }

        final double distanceMeter = DistanceCalculator.meters(
                region.getLatitude(), region.getLongitude(), request.getLatitude(), request.getLongitude());
        final boolean success = distanceMeter <= LOCATION_VERIFICATION_RADIUS_METER;
        locationVerificationRecorder.record(
                tripId, tripMissionId, request.getMemberId(),
                request.getLatitude(), request.getLongitude(), BigDecimal.valueOf(distanceMeter), success);
        if (!success) {
            throw new CommonException(ErrorCode.LOCATION_VERIFICATION_FAILED);
        }

        // 4. 완료 처리
        missionMapper.completeTripMission(tripId, tripMissionId);

        // 도감(미션 성공/FOOD/CAFE) 배지 지급을 위한 이벤트. 이 트랜잭션이 커밋된 뒤 별도 스레드에서 처리된다.
        // (완료가 롤백되면 배지 지급도 나가지 않고, 배지 처리 시간이 이 API 응답을 늦추지 않는다)
        final String missionType = missionMapper.selectMissionTypeByTripMission(tripMissionId);
        eventPublisher.publishEvent(new MissionCompletedEvent(tripId, tripMissionId, missionType));

        // 방금 완료한 미션으로 여행의 모든 날짜가 다 끝났다면 여행을 완료 처리한다.
        completeTripIfLastMission(tripId);
    }

    /**
     * 진행 중인 미션(tripMissionId)을 실패/포기 처리한다. 진행 중 상태가 아니면 예외를 던진다.
     */
    @Transactional
    public void failMission(final Long tripId, final Long tripMissionId) {
        final int failed = missionMapper.failTripMission(tripId, tripMissionId);
        if (failed == 0) {
            throw new CommonException(ErrorCode.NOT_FOUND_TRIP_MISSION);
        }

        // 포기도 "미션 수행"으로 간주한다. 방금 포기한 미션으로 여행의 모든 날짜가 다 끝났다면 여행을 완료 처리한다.
        completeTripIfLastMission(tripId);
    }

    /**
     * 이번에 완료/포기 처리로 여행의 모든 날짜가 남김없이 끝났는지 확인하고,
     * 맞다면 여행을 완료(COMPLETED) 처리한 뒤 {@link TripCompletedEvent} 를 발행한다.
     * "마지막 미션을 수행함(완료든 포기든)" = 마지막 날의 라운드까지 시작됐고, 지금까지 시작된 모든 날짜의
     * 목표 라운드 수가 하나도 빠짐없이 다 끝난 상태. 방금 처리한 미션이 어느 날짜(day_no) 것인지는 상관없다 -
     * 예를 들어 하루를 건너뛰고 마지막 날 미션들을 먼저 다 끝낸 뒤, 뒤늦게 건너뛴 날의 남은 미션을 처리하는
     * 경우에도 "그 처리로 인해 비로소 전부 끝났다"는 사실은 이 체크로 정확히 잡힌다.
     * 1. 여행의 총 일수(mission_start_at ~ end_date)를 계산한다.
     * 2. 마지막 날의 목표 라운드 수(target_round_count)가 아직 없으면(그 날짜가 시작도 안 됐으면) 당연히 아직이다.
     * 3. 지금까지 시작된 날짜 중 목표 라운드 수를 다 못 채운 날이 하나라도 있으면(마지막 날 포함) 아직이다.
     * 4. 위 조건을 모두 통과하면 여행을 완료 처리한다.
     *    (completeTrip 은 status = 'CREATED' 인 경우에만 반영되므로 중복 호출되어도 안전하다)
     */
    private void completeTripIfLastMission(final Long tripId) {
        final TripMissionSettingInfo trip = missionMapper.selectTripMissionSettingInfo(tripId);
        if (trip == null || trip.getMissionStartAt() == null || trip.getEndDate() == null) {
            return;
        }

        final int totalDays = (int) ChronoUnit.DAYS.between(
                trip.getMissionStartAt().toLocalDate(), trip.getEndDate().toLocalDate()) + 1;

        if (missionMapper.selectDailyGoal(tripId, totalDays) == null) {
            return;
        }

        if (missionMapper.countUnresolvedDays(tripId) > 0) {
            return;
        }

        final int completed = missionMapper.completeTrip(tripId);
        if (completed > 0) {
            eventPublisher.publishEvent(new TripCompletedEvent(tripId));
        }
    }

    /**
     * 이미 지난 날짜인데 다 못 끝낸 미션 라운드를 마감한다. (매일 자동 배치 전용 - MissionDailyCloseScheduler)
     * 1. 진행 중인 모든 여행을 통틀어, 달력 날짜가 이미 지났는데 목표 라운드 수를 다 못 채운 날짜를 찾는다.
     * 2. 그 날짜에 진행 중이던 미션(선택은 했지만 완료/포기 처리를 안 한 것)은 포기(FAILED) 처리한다.
     * 3. 그 날짜에 아직 뽑히지도 못한 나머지 라운드는, 목표 라운드 수를 낮추는 대신 "미션 수행 안함"
     *    (NOT_PERFORMED) 기록을 실제로 남긴다. (아직 선택된 적 없는 미션 중 무작위로 배정 - 이 라운드는
     *    진행되지 않았다는 이력만 남기고, resolvedRounds 계산상 "끝난 라운드"로 취급되게 한다)
     * 4. 이 마감으로 영향을 받은 여행들에 대해, 혹시 이걸로 여행 전체가 끝난 건지 다시 확인해 완료 처리한다.
     * 여러 여행/날짜를 한 번에 처리하는 배치이므로, 하나가 실패해도 나머지는 계속 진행한다.
     */
    public void closeStaleDailyGoals() {
        final List<StaleDailyGoal> staleDailyGoals = missionMapper.selectStaleDailyGoals();
        final Set<Long> affectedTripIds = new LinkedHashSet<>();

        for (final StaleDailyGoal stale : staleDailyGoals) {
            try {
                closeStaleDailyGoal(stale);
                affectedTripIds.add(stale.getTripId());
            } catch (final Exception e) {
                log.error("지난 날짜 마감 실패: tripId={}, dayNo={}, message={}",
                        stale.getTripId(), stale.getDayNo(), e.getMessage(), e);
            }
        }

        for (final Long tripId : affectedTripIds) {
            try {
                completeTripIfLastMission(tripId);
            } catch (final Exception e) {
                log.error("마감 후 여행 완료 판정 실패: tripId={}, message={}", tripId, e.getMessage(), e);
            }
        }

        log.info("지난 날짜 마감 배치 종료: 대상 날짜={}건, 영향받은 여행={}건",
                staleDailyGoals.size(), affectedTripIds.size());
    }

    /**
     * 지난 날짜 하나(stale)를 마감한다.
     * 1. 진행 중이던 라운드를 포기(FAILED) 처리한다.
     * 2. 실제로 생성된 라운드 수(완료/포기 포함)를 세어, 목표 라운드 수에서 모자란 만큼(missingCount)을 구한다.
     * 3. 모자란 만큼 이 여행에서 아직 선택되지 않은 미션을 무작위로 뽑아, 각각 "미션 수행 안함"
     *    (NOT_PERFORMED) 라운드로 남긴다. (뽑을 수 있는 미션이 모자라면 있는 만큼만 남기고 경고 로그를 남긴다)
     */
    private void closeStaleDailyGoal(final StaleDailyGoal stale) {
        final Long tripId = stale.getTripId();
        final int dayNo = stale.getDayNo();

        missionMapper.failInProgressTripMissionsForDay(tripId, dayNo);

        final int actualCount = missionMapper.countTripMissionsForDay(tripId, dayNo);
        final int missingCount = stale.getTargetRoundCount() - actualCount;
        if (missingCount <= 0) {
            return;
        }

        final TripMissionSettingInfo trip = missionMapper.selectTripMissionSettingInfo(tripId);
        if (trip == null || trip.getTripRegionId() == null) {
            log.error("미션 수행 안함 처리 실패 - 여행/지역 정보 없음: tripId={}, dayNo={}", tripId, dayNo);
            return;
        }

        final List<Long> missionIds =
                missionMapper.selectRandomMissionIds(missingCount, trip.getTripRegionId(), tripId);
        if (missionIds.size() < missingCount) {
            log.warn("미션 수행 안함 처리 - 배정 가능한 미션 부족: tripId={}, dayNo={}, 필요={}건, 확보={}건",
                    tripId, dayNo, missingCount, missionIds.size());
        }

        int assignedOrder = actualCount + 1;
        for (final Long missionId : missionIds) {
            missionMapper.insertNotPerformedTripMission(tripId, dayNo, assignedOrder, missionId);
            assignedOrder++;
        }
    }

    /**
     * 미션 후보(missionCandidateId)를 리롤한다. 후보당 1회만 가능하다.
     * 1. 리롤 가능한 후보인지 확인한다. (활성 + 미선택 + reroll_count > 0)
     * 2. 기존 후보는 비활성화한다. (rerolled_yn 'Y', mission_id 는 그대로 유지해 이력을 보존)
     * 3. 방금 버린 미션과 다르고, 이 여행에서 아직 선택된 적 없는 미션을 무작위로 1개 뽑는다.
     * 4. 새 후보를 같은 라운드에 저장한다. (reroll_count = 0, 다시 리롤 불가)
     * 5. 리롤 이력을 기록한다.
     */
    @Transactional
    public MissionCandidateResponse rerollMission(final Long tripId, final Long missionCandidateId, final Long memberId) {
        // 1. 리롤 가능한 후보인지 확인
        final MissionCandidateRerollInfo candidate = missionMapper.selectCandidateForReroll(tripId, missionCandidateId);
        if (candidate == null) {
            throw new CommonException(ErrorCode.REROLL_NOT_AVAILABLE);
        }

        // 2. 기존 후보 비활성화 (동시 리롤 경합 시 실패)
        final int deactivated = missionMapper.deactivateCandidateForReroll(tripId, missionCandidateId);
        if (deactivated == 0) {
            throw new CommonException(ErrorCode.REROLL_NOT_AVAILABLE);
        }

        // 3. 여행 지역 확인 후, 즉시 중복(방금 버린 미션) + 여행 전체 중복(이미 선택된 미션)을 피해 새 미션 조회
        final TripMissionSettingInfo trip = missionMapper.selectTripMissionSettingInfo(tripId);
        if (trip == null) {
            throw new CommonException(ErrorCode.NOT_FOUND_TRIP);
        }
        final MissionInfo newMission =
                missionMapper.selectRerollMission(tripId, trip.getTripRegionId(), candidate.getMissionId());
        if (newMission == null) {
            throw new CommonException(ErrorCode.NO_AVAILABLE_MISSION);
        }

        // 4. 새 후보를 같은 라운드에 저장 (reroll_count = 0, 다시 리롤 불가)
        final MissionRerollInsertParam param = MissionRerollInsertParam.builder()
                .tripId(tripId)
                .dayNo(candidate.getDayNo())
                .assignedOrder(candidate.getAssignedOrder())
                .missionId(newMission.getMissionId())
                .rerollCount(0)
                .build();
        missionMapper.insertRerolledCandidate(param);

        // 5. 리롤 이력 기록
        missionMapper.insertMissionRerollLog(
                tripId, candidate.getDayNo(), candidate.getAssignedOrder(),
                memberId, candidate.getMissionId(), newMission.getMissionId());

        return new MissionCandidateResponse(
                param.getMissionCandidateId(),
                newMission.getMissionId(),
                newMission.getMissionType(),
                newMission.getTitle(),
                newMission.getDescription(),
                newMission.getDifficulty(),
                "N",
                "N");
    }
}
