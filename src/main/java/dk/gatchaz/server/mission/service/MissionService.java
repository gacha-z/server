package dk.gatchaz.server.mission.service;

import dk.gatchaz.server.exception.CommonException;
import dk.gatchaz.server.exception.ErrorCode;
import dk.gatchaz.server.mission.dto.MissionCandidateListResponse;
import dk.gatchaz.server.mission.dto.MissionCandidateRerollInfo;
import dk.gatchaz.server.mission.dto.MissionCandidateResponse;
import dk.gatchaz.server.mission.dto.MissionCandidateSelectionInfo;
import dk.gatchaz.server.mission.dto.MissionCompleteRequest;
import dk.gatchaz.server.mission.dto.MissionInfo;
import dk.gatchaz.server.mission.dto.MissionRerollInsertParam;
import dk.gatchaz.server.mission.dto.MissionSelectParam;
import dk.gatchaz.server.mission.dto.MissionSelectResponse;
import dk.gatchaz.server.mission.dto.TripMissionSettingInfo;
import dk.gatchaz.server.mission.dto.TripRegionCoordinate;
import dk.gatchaz.server.mission.mapper.MissionMapper;
import dk.gatchaz.server.mission.support.DistanceCalculator;
import dk.gatchaz.server.type.EMissionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class MissionService {

    /** 라운드(미션 1회)당 생성되는 후보 개수 */
    private static final int CANDIDATE_COUNT = 3;

    /** 미션 완료 시 위치 인증 허용 거리(미터). 여행 지역 중심 좌표 기준. */
    private static final double LOCATION_VERIFICATION_RADIUS_METER = 10_000.0;

    private final MissionMapper missionMapper;
    private final LocationVerificationRecorder locationVerificationRecorder;

    /**
     * 현재 라운드의 미션 후보 3개를 조회한다. 아직 생성되지 않은 라운드면 새로 생성해서 반환한다.
     * 1. 여행 존재 여부, 지역 선택 여부, 미션 시작 여부(mission_start_at 도달)를 확인한다.
     * 2. mission_start_at 날짜 기준으로 오늘이 몇 일차(dayNo)인지 계산한다. (시작일 = 1일차)
     * 3. 그 날의 목표 라운드 수(target_round_count)가 없으면 mission_min~mission_max 사이 무작위 값으로
     *    생성해 저장한다. (하루 동안 고정)
     * 4. 완료/실패로 끝난 라운드 수(resolvedRounds)로 현재 라운드 번호를 정한다. (resolvedRounds + 1)
     *    이미 목표 라운드 수만큼 다 끝났으면 오늘은 더 이상 라운드가 없다.
     * 5. 현재 라운드의 활성 후보(rerolled_yn = false)를 조회한다.
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
    }

    /**
     * 미션 후보(missionCandidateId)를 리롤한다. 후보당 1회만 가능하다.
     * 1. 리롤 가능한 후보인지 확인한다. (활성 + 미선택 + reroll_count > 0)
     * 2. 기존 후보는 비활성화한다. (rerolled_yn true, mission_id 는 그대로 유지해 이력을 보존)
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
                false);
    }
}
