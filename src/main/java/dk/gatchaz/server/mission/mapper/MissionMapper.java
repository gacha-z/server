package dk.gatchaz.server.mission.mapper;

import dk.gatchaz.server.mission.dto.MissionCandidateRerollInfo;
import dk.gatchaz.server.mission.dto.MissionCandidateResponse;
import dk.gatchaz.server.mission.dto.MissionCandidateSelectionInfo;
import dk.gatchaz.server.mission.dto.MissionInfo;
import dk.gatchaz.server.mission.dto.MissionRerollInsertParam;
import dk.gatchaz.server.mission.dto.MissionSelectParam;
import dk.gatchaz.server.mission.dto.StaleDailyGoal;
import dk.gatchaz.server.mission.dto.TripMissionSettingInfo;
import dk.gatchaz.server.mission.dto.TripRegionCoordinate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface MissionMapper {

    /**
     * 여행(tripId)의 미션 관련 설정(선택된 지역, 하루 최소·최대 미션 수, 첫 미션 시작 일시)을 조회한다. 없으면 null.
     */
    TripMissionSettingInfo selectTripMissionSettingInfo(@Param("tripId") Long tripId);

    /**
     * 여행(tripId)의 특정 일자(dayNo)에 대한 목표 라운드 수(target_round_count)를 조회한다. 없으면 null.
     */
    Integer selectDailyGoal(@Param("tripId") Long tripId, @Param("dayNo") int dayNo);

    /**
     * 여행(tripId)의 특정 일자(dayNo)의 목표 라운드 수(targetRoundCount)를 저장한다.
     * mission_min~mission_max 사이에서 정해진 값으로, 그 날 하루 동안 고정된다.
     */
    int insertDailyGoal(@Param("tripId") Long tripId,
                         @Param("dayNo") int dayNo,
                         @Param("targetRoundCount") int targetRoundCount);

    /**
     * 여행(tripId)의 특정 일자(dayNo)에 완료(COMPLETED) 또는 실패(FAILED)로 종료된 라운드 수를 조회한다.
     * (다음 라운드 번호 = 이 값 + 1, 이 값이 목표 라운드 수 이상이면 오늘 미션을 모두 소진한 것)
     */
    int countResolvedRounds(@Param("tripId") Long tripId, @Param("dayNo") int dayNo);

    /**
     * 여행(tripId)의 특정 일자(dayNo), 특정 라운드(assignedOrder)의 활성 후보(rerolled_yn = 'N')를 조회한다.
     * 아직 후보가 생성되지 않은 라운드면 빈 리스트를 반환한다.
     */
    List<MissionCandidateResponse> selectActiveCandidates(@Param("tripId") Long tripId,
                                                           @Param("dayNo") int dayNo,
                                                           @Param("assignedOrder") int assignedOrder);

    /**
     * 사용 가능한(use_yn = 'Y') 미션 중 해당 지역(tripRegionId)에 속하면서,
     * 이 여행(tripId)에서 이미 선택(mission_candidate.selected_yn = 'Y')된 적 없는 미션을
     * 무작위로 cnt 개수만큼 조회한다.
     * (리롤로 버려진 미션은 선택된 것이 아니므로 이후 라운드에 다시 나올 수 있다 - 즉시 리롤 중복 방지만 별도 처리)
     */
    List<Long> selectRandomMissionIds(@Param("cnt") int cnt,
                                       @Param("tripRegionId") Long tripRegionId,
                                       @Param("tripId") Long tripId);

    /**
     * 새로 뽑힌 미션 목록을 해당 라운드(dayNo, assignedOrder)의 후보로 mission_candidate 에 저장한다.
     * (selected_yn = 'N', rerolled_yn = 'N') pickerMemberId 는 이 라운드에서 선택/리롤할 수 있는
     * 담당자로, 후보 3개 모두에 동일하게 기록된다.
     */
    int insertCandidates(@Param("tripId") Long tripId,
                          @Param("dayNo") int dayNo,
                          @Param("assignedOrder") int assignedOrder,
                          @Param("missionIds") List<Long> missionIds,
                          @Param("pickerMemberId") Long pickerMemberId);

    /**
     * 여행(tripId)에 현재 참여(JOINED) 중인 팀원 중 1명을 무작위로 조회한다. (라운드 담당자 배정용) 없으면 null.
     */
    Long selectRandomJoinedMemberId(@Param("tripId") Long tripId);

    /**
     * 해당 라운드(tripId, dayNo, assignedOrder)의 담당자(picker_member_id)를 조회한다. 없으면 null.
     */
    Long selectPickerMemberId(@Param("tripId") Long tripId,
                              @Param("dayNo") int dayNo,
                              @Param("assignedOrder") int assignedOrder);

    /**
     * 선택(select) 대상 후보(missionCandidateId)의 정보를 조회한다.
     * 해당 여행(tripId)의 활성(rerolled_yn = 'N') + 미선택(selected_yn = 'N') 후보가 아니면 null.
     */
    MissionCandidateSelectionInfo selectCandidateForSelection(@Param("tripId") Long tripId,
                                                               @Param("missionCandidateId") Long missionCandidateId);

    /**
     * 후보(missionCandidateId)를 선택 확정한다. (selected_yn 'N' -> 'Y')
     * 이미 선택되었거나 대상이 없으면 0을 반환한다. (동시 선택 경합 방지용 가드)
     */
    int markCandidateSelected(@Param("tripId") Long tripId, @Param("missionCandidateId") Long missionCandidateId);

    /**
     * 선택된 미션을 진행 중(trip_mission, status = 'IN_PROGRESS')으로 새로 생성한다.
     * 생성된 trip_mission_id 는 param.tripMissionId 에 채워진다.
     */
    int insertTripMission(MissionSelectParam param);

    /**
     * 여행(tripId)에서 진행 중(status = 'IN_PROGRESS')인 특정 trip_mission(tripMissionId)이 존재하는지 확인한다.
     * (존재하면 1 이상)
     */
    int existsInProgressTripMission(@Param("tripId") Long tripId, @Param("tripMissionId") Long tripMissionId);

    /**
     * 여행(tripId)에 현재 참여(JOINED) 중인 팀원 수를 조회한다.
     */
    int countJoinedMembers(@Param("tripId") Long tripId);

    /**
     * 회원(memberId)이 해당 여행(tripId)에 현재 참여(JOINED) 중인지 확인한다. (참여 중이면 1 이상)
     */
    int existsJoinedMember(@Param("tripId") Long tripId, @Param("memberId") Long memberId);

    /**
     * 특정 진행 미션(tripMissionId)에 대해 셋로그를 촬영한 서로 다른 팀원 수를 조회한다. (삭제되지 않은 것만)
     */
    int countSetlogMembers(@Param("tripMissionId") Long tripMissionId);

    /**
     * 여행(tripId)이 선택한 지역(trip_region)의 중심 좌표를 조회한다. 없으면 null.
     */
    TripRegionCoordinate selectTripRegionCoordinate(@Param("tripId") Long tripId);

    /**
     * trip_mission 이 어떤 mission_type 의 미션인지 조회한다. (도감 배지 지급 판단용)
     */
    String selectMissionTypeByTripMission(@Param("tripMissionId") Long tripMissionId);

    /**
     * 위치 인증 시도 이력을 location_verification_log 에 기록한다. (성공/실패 모두 기록)
     * 좌표를 비교할 수 없었던 경우(여행 지역 좌표 없음 등) distanceMeter 는 null 일 수 있다.
     */
    int insertLocationVerificationLog(@Param("tripId") Long tripId,
                                       @Param("tripMissionId") Long tripMissionId,
                                       @Param("memberId") Long memberId,
                                       @Param("latitude") BigDecimal latitude,
                                       @Param("longitude") BigDecimal longitude,
                                       @Param("distanceMeter") BigDecimal distanceMeter,
                                       @Param("successYn") String successYn);

    /**
     * 진행 중인 미션(trip_mission)을 완료 처리한다. (status 'IN_PROGRESS' -> 'COMPLETED')
     * 진행 중 상태가 아니면 0을 반환한다.
     */
    int completeTripMission(@Param("tripId") Long tripId, @Param("tripMissionId") Long tripMissionId);

    /**
     * 진행 중인 미션(trip_mission)을 실패/포기 처리한다. (status 'IN_PROGRESS' -> 'FAILED')
     * 진행 중 상태가 아니면 0을 반환한다.
     */
    int failTripMission(@Param("tripId") Long tripId, @Param("tripMissionId") Long tripMissionId);

    /**
     * 여행(tripId)에서 목표 라운드 수(mission_daily_goal.target_round_count)가 아직 다 채워지지 않은
     * (완료/실패로 끝난 라운드 수가 목표에 못 미치는) 날짜(day_no)의 개수를 센다. (여행 완료 판정용 - 0이어야
     * 지금까지 시작한 모든 날짜가 남김없이 다 끝난 것이다)
     */
    int countUnresolvedDays(@Param("tripId") Long tripId);

    // ------------------------------------------------------------------
    // 지난 날짜 자동 마감 배치용 (MissionDailyCloseScheduler 에서만 사용)
    // ------------------------------------------------------------------

    /**
     * 진행 중(status = 'CREATED')인 모든 여행을 통틀어, day_no 에 해당하는 달력 날짜가 이미 지났는데
     * 목표 라운드 수를 다 못 채운 mission_daily_goal 을 전부 조회한다.
     */
    List<StaleDailyGoal> selectStaleDailyGoals();

    /**
     * 여행(tripId)의 특정 일자(dayNo)에 진행 중(IN_PROGRESS)인 trip_mission 을 전부 포기(FAILED) 처리한다.
     * (그 라운드는 뽑아서 진행하다가 날짜가 넘어가도록 방치된 것이므로 포기로 간주한다)
     */
    int failInProgressTripMissionsForDay(@Param("tripId") Long tripId, @Param("dayNo") int dayNo);

    /**
     * 여행(tripId)의 특정 일자(dayNo)에 실제로 생성된 trip_mission 로우 수를 센다. (상태 무관, 전체)
     * 목표 라운드 수 대비 몇 라운드가 아예 뽑히지도 못했는지 계산하는 데 쓰인다.
     */
    int countTripMissionsForDay(@Param("tripId") Long tripId, @Param("dayNo") int dayNo);

    /**
     * 후보로 뽑히지도 못한 채 날짜가 지나버린 라운드(assignedOrder)에 "미션 수행 안함"(NOT_PERFORMED) 기록을
     * 남긴다. 실제로 후보로 노출된 적은 없지만, 이 여행에서 아직 선택되지 않은 미션 중 하나(missionId)를
     * 배정해 "이 라운드는 진행되지 않았다"는 이력만 남긴다.
     */
    int insertNotPerformedTripMission(@Param("tripId") Long tripId,
                                       @Param("dayNo") int dayNo,
                                       @Param("assignedOrder") int assignedOrder,
                                       @Param("missionId") Long missionId);

    /**
     * 여행(tripId)을 완료 처리한다. (status 'CREATED' -> 'COMPLETED')
     * 마지막 날의 마지막 라운드 미션까지 완료/포기로 끝났을 때만 호출된다.
     * 이미 완료/취소된 여행이면 0을 반환한다. (중복 호출에 안전)
     */
    int completeTrip(@Param("tripId") Long tripId);

    /**
     * 리롤(reroll) 대상 후보(missionCandidateId)의 정보를 조회한다.
     * 해당 여행(tripId)의 활성(rerolled_yn = 'N') + 미선택(selected_yn = 'N') + 리롤 가능(reroll_count > 0)
     * 후보가 아니면 null.
     */
    MissionCandidateRerollInfo selectCandidateForReroll(@Param("tripId") Long tripId,
                                                         @Param("missionCandidateId") Long missionCandidateId);

    /**
     * 리롤 대상 후보(missionCandidateId)를 비활성화한다. (rerolled_yn 'N' -> 'Y', mission_id 는 그대로 유지해 이력을 보존한다)
     * 리롤 가능 상태가 아니면(이미 선택/리롤되었거나 횟수 소진) 0을 반환한다.
     */
    int deactivateCandidateForReroll(@Param("tripId") Long tripId, @Param("missionCandidateId") Long missionCandidateId);

    /**
     * 사용 가능한(use_yn = 'Y') 미션 중 해당 지역(tripRegionId)에 속하면서,
     * 이 여행(tripId)에서 이미 선택된 적 없고, 방금 리롤로 버려진 미션(excludeMissionId)과도 다른
     * 미션을 무작위로 1개 조회한다. (즉시 리롤 중복 방지 + 여행 전체 선택 중복 방지)
     */
    MissionInfo selectRerollMission(@Param("tripId") Long tripId,
                                     @Param("tripRegionId") Long tripRegionId,
                                     @Param("excludeMissionId") Long excludeMissionId);

    /**
     * 리롤로 새로 뽑힌 미션을 같은 라운드(dayNo, assignedOrder)의 활성 후보로 저장한다.
     * (selected_yn = 'N', rerolled_yn = 'N', reroll_count = 0 - 다시 리롤할 수 없음)
     * 생성된 mission_candidate_id 는 param.missionCandidateId 에 채워진다.
     */
    int insertRerolledCandidate(MissionRerollInsertParam param);

    /**
     * 리롤 이력을 mission_reroll_log 에 기록한다.
     */
    int insertMissionRerollLog(@Param("tripId") Long tripId,
                                @Param("dayNo") int dayNo,
                                @Param("assignedOrder") int assignedOrder,
                                @Param("memberId") Long memberId,
                                @Param("oldMissionId") Long oldMissionId,
                                @Param("newMissionId") Long newMissionId);
}
