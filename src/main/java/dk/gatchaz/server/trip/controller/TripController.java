package dk.gatchaz.server.trip.controller;

import dk.gatchaz.server.dto.ResponseDto;
import dk.gatchaz.server.trip.dto.TripCreateRequest;
import dk.gatchaz.server.trip.dto.TripCreateResponse;
import dk.gatchaz.server.trip.dto.TripInviteCodeResponse;
import dk.gatchaz.server.trip.dto.TripJoinRequest;
import dk.gatchaz.server.trip.dto.TripJoinResponse;
import dk.gatchaz.server.trip.dto.TripRegionDto;
import dk.gatchaz.server.trip.dto.TripRegionSelectRequest;
import dk.gatchaz.server.trip.dto.TripRerollRequest;
import dk.gatchaz.server.trip.service.TripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Trip", description = "여행 API")
@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    /**
     * 여행 생성
     */
    @Operation(summary = "여행 생성", description = "화면 입력값으로 여행을 생성하고 생성자를 OWNER 로 등록한다. 지역은 이 단계에서 선택하지 않으며(trip_region_id 는 NULL), 반환된 tripId 로 추천/리롤 후 지역 선택 API 를 호출한다.")
    @PostMapping
    public ResponseDto<TripCreateResponse> createTrip(@Valid @RequestBody final TripCreateRequest request) {
        return ResponseDto.created(tripService.createTrip(request));
    }

    /**
     * 최종 선택한 여행 지역 확정
     */
    @Operation(summary = "여행 지역 선택", description = "사용자가 최종 선택한 지역(tripRegionId)을 여행(tripId)에 반영한다. 해당 후보를 selected_yn='Y'로 확정하고 trip_region_id 를 저장한다. 하나라도 실패하면 전체 롤백된다.")
    @PatchMapping("/regions/select")
    public ResponseDto<TripCreateResponse> selectTripRegion(@Valid @RequestBody final TripRegionSelectRequest request) {
        return ResponseDto.ok(tripService.selectTripRegion(request.getTripId(), request.getTripRegionId()));
    }

    /**
     * 여행 생성 시 랜덤 지역 3개 추천
     */
    @Operation(summary = "랜덤 여행 지역 3개 추천", description = "use_yn = 'Y' 이고 해당 여행에서 아직 선택되지 않은 지역 중 무작위로 3개를 조회한다.")
    @GetMapping("/regions/random")
    public ResponseDto<List<TripRegionDto>> getRandomRegions(@RequestParam final Long tripId) {
        return ResponseDto.ok(tripService.getRandomRegions(tripId));
    }

    /**
     * 추천된 후보 1개를 다른 지역으로 리롤(교체)
     */
    @Operation(summary = "추천 여행 지역 리롤", description = "특정 후보(tripCandidateId)를 기존 후보와 중복되지 않는 새 지역으로 교체한다. 후보당 1회만 가능하다.")
    @PatchMapping("/regions/reroll")
    public ResponseDto<TripRegionDto> rerollRegion(@Valid @RequestBody final TripRerollRequest request) {
        return ResponseDto.ok(tripService.rerollRegion(request.getTripId(), request.getTripCandidateId()));
    }

    /**
     * 여행 초대 코드 조회
     */
    @Operation(summary = "여행 초대 코드 조회", description = "여행 생성 시 발급된 만료 없는 초대 코드를 반환한다. 프론트에서 도메인을 붙여 링크로 사용한다. (예: travel-gacha.app/trip/{code})")
    @GetMapping("/{tripId}/invite-code")
    public ResponseDto<TripInviteCodeResponse> getInviteCode(@PathVariable final Long tripId) {
        return ResponseDto.ok(tripService.getInviteCode(tripId));
    }

    /**
     * 초대 링크로 여행 참여
     */
    @Operation(summary = "여행 참여", description = "초대 링크의 코드로 회원을 여행에 참여시킨다. 유효하지 않은 코드, 참여 불가 상태, 이미 참여, 정원 초과 시 실패한다.")
    @PostMapping("/join")
    public ResponseDto<TripJoinResponse> joinTrip(@Valid @RequestBody final TripJoinRequest request) {
        return ResponseDto.ok(tripService.joinTrip(request.getCode(), request.getMemberId()));
    }
}
