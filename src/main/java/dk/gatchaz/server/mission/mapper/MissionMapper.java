package dk.gatchaz.server.mission.mapper;

import dk.gatchaz.server.mission.dto.MissionCandidateRerollInfo;
import dk.gatchaz.server.mission.dto.MissionCandidateResponse;
import dk.gatchaz.server.mission.dto.MissionCandidateSelectionInfo;
import dk.gatchaz.server.mission.dto.MissionInfo;
import dk.gatchaz.server.mission.dto.MissionRerollInsertParam;
import dk.gatchaz.server.mission.dto.MissionSelectParam;
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
     * 여행(tripId)의 특정 일자(dayNo), 특정 라운드(assignedOrder)의 활성 후보(rerolled_yn = false)를 조회한다.
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
     * (selected_yn = 'N', rerolled_yn = false)
     */
    int insertCandidates(@Param("tripId") Long tripId,
                          @Param("dayNo") int dayNo,
                          @Param("assignedOrder") int assignedOrder,
                          @Param("missionIds") List<Long> missionIds);

    /**
     * 선택(select) 대상 후보(missionCandidateId)의 정보를 조회한다.
     * 해당 여행(tripId)의 활성(rerolled_yn = false) + 미선택(selected_yn = 'N') 후보가 아니면 null.
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
     * 특정 진행 미션(tripMissionId)에 대해 셋로그를 촬영한 서로 다른 팀원 수를 조회한다. (삭제되지 않은 것만)
     */
    int countSetlogMembers(@Param("tripMissionId") Long tripMissionId);

    /**
     * 여행(tripId)이 선택한 지역(trip_region)의 중심 좌표를 조회한다. 없으면 null.
     */
    TripRegionCoordinate selectTripRegionCoordinate(@Param("tripId") Long tripId);

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
     * 리롤(reroll) 대상 후보(missionCandidateId)의 정보를 조회한다.
     * 해당 여행(tripId)의 활성(rerolled_yn = false) + 미선택(selected_yn = 'N') + 리롤 가능(reroll_count > 0)
     * 후보가 아니면 null.
     */
    MissionCandidateRerollInfo selectCandidateForReroll(@Param("tripId") Long tripId,
                                                         @Param("missionCandidateId") Long missionCandidateId);

    /**
     * 리롤 대상 후보(missionCandidateId)를 비활성화한다. (rerolled_yn false -> true, mission_id 는 그대로 유지해 이력을 보존한다)
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
     * (selected_yn = 'N', rerolled_yn = false, reroll_count = 0 - 다시 리롤할 수 없음)
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
