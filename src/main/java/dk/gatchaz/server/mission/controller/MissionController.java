package dk.gatchaz.server.mission.controller;

import dk.gatchaz.server.dto.ResponseDto;
import dk.gatchaz.server.mission.dto.MissionCandidateListResponse;
import dk.gatchaz.server.mission.dto.MissionCompleteRequest;
import dk.gatchaz.server.mission.dto.MissionSelectResponse;
import dk.gatchaz.server.mission.service.MissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Mission", description = "미션 API")
@RestController
@RequestMapping("/api/v1/trips/{tripId}/missions")
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;

    /**
     * 미션 후보 조회 (없으면 생성)
     */
    @Operation(
            summary = "미션 후보 조회",
            description = """
                    현재 라운드의 미션 후보 3개를 조회한다. 아직 생성되지 않은 라운드면 새로 생성해서 반환한다.

                    - 여행의 mission_start_at 날짜를 기준으로 오늘이 몇 일차(dayNo)인지 계산한다. (시작일 = 1일차)
                    - 그 날의 목표 라운드 수(targetRoundCount)가 아직 정해지지 않았으면 mission_min~mission_max
                      사이에서 무작위로 정해 하루 동안 고정한다.
                    - 완료/실패로 끝난 라운드 수를 기준으로 현재 라운드 번호를 정하고, 목표 라운드를 다 채웠으면
                      더 이상 후보를 만들지 않는다.
                    - 여행 지역(trip_region_id)의 미션 중, 이 여행에서 이미 선택(selected_yn='Y')된 적 없는
                      미션 3개를 무작위로 뽑아 후보로 저장한다. (리롤로 버려진 후보는 이후 라운드에 다시 나올 수 있음)

                    ### 실패 응답
                    - 여행이 없으면 **404** (NOT_FOUND_TRIP)
                    - 아직 여행 지역이 선택되지 않았으면 **400** (TRIP_REGION_NOT_SELECTED)
                    - 아직 미션 시작 시각(mission_start_at) 전이면 **400** (MISSION_NOT_STARTED)
                    - 오늘 목표 라운드를 모두 완료/실패 처리했으면 **409** (DAILY_MISSION_QUOTA_COMPLETED)
                    - 추천 가능한 미션이 3개 미만이면 **400** (NO_AVAILABLE_MISSION)
                    """)
    @GetMapping("/candidates")
    public ResponseDto<MissionCandidateListResponse> getMissionCandidates(
            @Parameter(description = "여행 ID", example = "1") @PathVariable final Long tripId) {
        return ResponseDto.ok(missionService.getMissionCandidates(tripId));
    }

    /**
     * 미션 후보 선택
     */
    @Operation(
            summary = "미션 선택",
            description = """
                    미션 후보(missionCandidateId) 중 하나를 선택해 진행 중인 미션으로 확정한다.

                    - 선택한 후보를 selected_yn='Y'로 확정하고, trip_mission 을 status='IN_PROGRESS' 로 새로 생성한다.
                    - 이미 선택되었거나, 리롤되어 비활성화된 후보, 다른 여행/라운드의 후보는 선택할 수 없다.
                    """)
    @PostMapping("/{missionCandidateId}/select")
    public ResponseDto<MissionSelectResponse> selectMission(
            @Parameter(description = "여행 ID", example = "1") @PathVariable final Long tripId,
            @Parameter(description = "선택할 미션 후보 ID", example = "1") @PathVariable final Long missionCandidateId) {
        return ResponseDto.ok(missionService.selectMission(tripId, missionCandidateId));
    }

    /**
     * 미션 완료
     */
    @Operation(
            summary = "미션 완료",
            description = """
                    진행 중인 미션(tripMissionId)을 완료 처리한다.

                    - 현재 참여 중인 모든 팀원이 각자 셋로그를 촬영해야 완료할 수 있다. (전원 촬영 필수)
                    - 완료 버튼을 누른 시점의 좌표(latitude, longitude)와 여행 지역 중심 좌표 사이 거리가
                      10km 이내여야 위치 인증이 통과된다.
                    - 위치 인증에 실패해도 진행 중 상태는 유지되어 다시 시도할 수 있다.

                    ### 실패 응답
                    - 진행 중인 미션이 아니면 **404** (NOT_FOUND_TRIP_MISSION)
                    - 팀원 전원이 셋로그를 촬영하지 않았으면 **400** (SETLOG_NOT_COMPLETE)
                    - 위치 인증에 실패했으면(허용 거리 초과) **400** (LOCATION_VERIFICATION_FAILED)
                    """)
    @PostMapping("/{tripMissionId}/complete")
    public ResponseDto<Void> completeMission(
            @Parameter(description = "여행 ID", example = "1") @PathVariable final Long tripId,
            @Parameter(description = "완료 처리할 진행 미션(trip_mission) ID", example = "1") @PathVariable final Long tripMissionId,
            @Valid @RequestBody final MissionCompleteRequest request) {
        missionService.completeMission(tripId, tripMissionId, request);
        return ResponseDto.<Void>ok(null);
    }

    /**
     * 미션 실패/포기
     */
    @Operation(summary = "미션 실패/포기", description = "진행 중인 미션(tripMissionId)을 실패/포기 처리한다. (status 'IN_PROGRESS' -> 'FAILED') 진행 중인 미션이 아니면 404 를 반환한다.")
    @PostMapping("/{tripMissionId}/fail")
    public ResponseDto<Void> failMission(
            @Parameter(description = "여행 ID", example = "1") @PathVariable final Long tripId,
            @Parameter(description = "실패 처리할 진행 미션(trip_mission) ID", example = "1") @PathVariable final Long tripMissionId) {
        missionService.failMission(tripId, tripMissionId);
        return ResponseDto.<Void>ok(null);
    }
}
