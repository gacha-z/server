package dk.gatchaz.server.setlog.controller;

import dk.gatchaz.server.common.annotation.UserId;
import dk.gatchaz.server.common.dto.ResponseDto;
import dk.gatchaz.server.setlog.dto.SetlogDownloadResponse;
import dk.gatchaz.server.setlog.dto.SetlogResponse;
import dk.gatchaz.server.setlog.dto.SetlogUploadResponse;
import dk.gatchaz.server.setlog.service.SetlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Setlog", description = "셋로그 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SetlogController {

    private final SetlogService setlogService;

    /**
     * 셋로그 업로드
     */
    @Operation(
            summary = "셋로그 업로드",
            description = """
                    미션 수행 중 촬영한 영상을 업로드한다. multipart/form-data 로 받으며, 서버가 영상을
                    S3 에 저장하고 그 URL을 DB에 기록한다. (허용 확장자 mp4/mov, 최대 100MB)

                    - 렌더링 위치(slotNo)는 서버가 업로드 순서대로 자동 배정한다.
                    - 한 진행 미션(tripMissionId)에 같은 팀원이 두 번 업로드할 수 없다. (재업로드 불가)

                    ### 실패 응답
                    - 진행 중인 미션이 아니면 **404** (NOT_FOUND_TRIP_MISSION)
                    - 참여 중인 팀원이 아니면 **404** (NOT_FOUND_TRIP_MEMBER)
                    - 이미 이 미션에 셋로그를 등록했으면 **409** (ALREADY_EXISTS_SETLOG)
                    - 허용되지 않은 확장자면 **400** (UNSUPPORTED_MEDIA_TYPE)
                    """)
    @PostMapping(value = "/setlogs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseDto<SetlogUploadResponse> uploadSetlog(
            @Parameter(description = "여행 ID", example = "1") @RequestParam final Long tripId,
            @Parameter(description = "진행 미션(trip_mission) ID", example = "1") @RequestParam final Long tripMissionId,
            @UserId final Long userId,
            @Parameter(description = "업로드할 영상 파일 (mp4/mov, 최대 100MB)")
            @RequestParam("file") final MultipartFile file) {
        return ResponseDto.created(setlogService.uploadSetlog(tripId, tripMissionId, userId, file));
    }

    /**
     * 여행별 셋로그 목록 조회
     */
    @Operation(summary = "여행별 셋로그 목록 조회", description = "여행(tripId)의 전체 셋로그 목록을 진행 미션 순, 슬롯 순으로 조회한다. (여행 기록/진행 중 화면 공통)")
    @GetMapping("/trips/{tripId}/setlogs")
    public ResponseDto<List<SetlogResponse>> getSetlogsByTrip(
            @Parameter(description = "여행 ID", example = "1") @PathVariable final Long tripId) {
        return ResponseDto.ok(setlogService.getSetlogsByTrip(tripId));
    }

    /**
     * 미션별 셋로그 조회
     */
    @Operation(summary = "미션별 셋로그 조회", description = "특정 진행 미션(tripMissionId)의 셋로그 목록을 슬롯 순으로 조회한다. (미션 완료 검증/촬영 현황 확인용)")
    @GetMapping("/missions/{tripMissionId}/setlogs")
    public ResponseDto<List<SetlogResponse>> getSetlogsByMission(
            @Parameter(description = "진행 미션(trip_mission) ID", example = "1") @PathVariable final Long tripMissionId) {
        return ResponseDto.ok(setlogService.getSetlogsByMission(tripMissionId));
    }

    /**
     * 셋로그 다운로드
     */
    @Operation(summary = "셋로그 다운로드", description = "셋로그(setlogId)의 영상 URL을 반환한다. 다운로드 시도 이력이 setlog_download_log 에 기록된다. 없는 셋로그면 404 를 반환한다.")
    @GetMapping("/setlogs/{setlogId}/download")
    public ResponseDto<SetlogDownloadResponse> downloadSetlog(
            @Parameter(description = "셋로그 ID", example = "1") @PathVariable final Long setlogId,
            @UserId final Long userId) {
        return ResponseDto.ok(setlogService.downloadSetlog(setlogId, userId));
    }
}
