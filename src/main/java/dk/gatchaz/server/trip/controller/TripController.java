package dk.gatchaz.server.trip.controller;

import dk.gatchaz.server.dto.ResponseDto;
import dk.gatchaz.server.trip.dto.TripCreateRequest;
import dk.gatchaz.server.trip.dto.TripCreateResponse;
import dk.gatchaz.server.trip.dto.TripDetailResponse;
import dk.gatchaz.server.trip.dto.TripInviteCodeResponse;
import dk.gatchaz.server.trip.dto.TripJoinRequest;
import dk.gatchaz.server.trip.dto.TripJoinResponse;
import dk.gatchaz.server.trip.dto.TripListResponse;
import dk.gatchaz.server.trip.dto.TripRegionDto;
import dk.gatchaz.server.trip.dto.TripRegionSelectRequest;
import dk.gatchaz.server.trip.dto.TripRerollRequest;
import dk.gatchaz.server.trip.dto.TripSearchRequest;
import dk.gatchaz.server.trip.service.TripService;
import dk.gatchaz.server.type.ETripStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
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
     * 내가 참여한 여행 목록 조회 (이름/지역/기간/상태 필터, 무한 스크롤)
     */
    @Operation(
            summary = "여행 목록 조회",
            description = """
                    로그인한 회원이 참여(JOINED)한 여행을 검색 조건으로 필터링하여 조회한다. (최신순, 커서 기반 무한 스크롤)

                    ### 필터 (모두 선택값, 없으면 조건 무시)
                    - **title**: 여행 이름 부분 일치 (예: `제주` → "제주 여행", "여름 제주" 모두 검색)
                    - **tripRegionId**: 지역 ID 정확 일치. 지역 미선택 여행은 이 조건을 주면 제외된다.
                    - **status**: 여행 상태 (CREATED / CANCELLED / COMPLETED)
                    - **dateFrom, dateTo**: 검색 기간. 여행 기간(startDate~endDate)이 이 구간과 **겹치면** 조회된다.
                      (한쪽만 줘도 됨. 조건식: `endDate >= dateFrom AND startDate <= dateTo`)

                    ### 무한 스크롤 (커서 방식)
                    1. 첫 조회는 `cursor` 없이 호출한다.
                    2. 응답의 `nextCursor` 를 그대로 다음 요청의 `cursor` 로 넘긴다.
                    3. `hasNext = false` (이때 `nextCursor = null`) 이면 마지막 페이지이므로 추가 호출을 멈춘다.

                    필터 조건은 스크롤 중 동일하게 유지하고 `cursor` 만 갱신한다.
                    """)
    @GetMapping
    public ResponseDto<TripListResponse> getTrips(
            @Parameter(description = "여행 이름 (부분 일치 검색)", example = "제주")
            @RequestParam(required = false) final String title,
            @Parameter(description = "여행 지역 ID (정확히 일치). 지역 미선택 여행은 이 값을 주면 제외된다.", example = "5")
            @RequestParam(required = false) final Long tripRegionId,
            @Parameter(description = "여행 상태 (CREATED: 생성됨 / CANCELLED: 취소됨 / COMPLETED: 완료됨)", example = "CREATED")
            @RequestParam(required = false) final ETripStatus status,
            @Parameter(description = "검색 기간 시작일 (yyyy-MM-dd). 여행 기간이 dateFrom~dateTo 와 겹치면 조회된다.", example = "2026-07-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate dateFrom,
            @Parameter(description = "검색 기간 종료일 (yyyy-MM-dd)", example = "2026-07-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate dateTo,
            @Parameter(description = "무한 스크롤 커서. 이전 응답의 nextCursor 값을 넣는다. 첫 조회 시 비운다.", example = "42")
            @RequestParam(required = false) final Long cursor,
            @Parameter(description = "한 번에 조회할 개수 (기본 10, 1~50 범위를 벗어나면 자동 보정)", example = "10")
            @RequestParam(required = false, defaultValue = "10") final int size,
            @Parameter(description = "조회 기준 회원 ID. 로그인 연동 전까지 요청으로 받는 선택값이다.", example = "1")
            @RequestParam(required = false) final Long memberId) {
        final TripSearchRequest request =
                new TripSearchRequest(title, tripRegionId, status, dateFrom, dateTo, cursor, size, memberId);
        return ResponseDto.ok(tripService.getTrips(request));
    }

    /**
     * 여행 상세 조회
     */
    @Operation(
            summary = "여행 상세 조회",
            description = """
                    여행 ID로 단건의 상세 정보를 조회한다. 없는 여행이면 404 를 반환한다.

                    - 여행 기본 정보(이름/기간/정원/상태)와 미션 설정(하루 최소·최대 미션 수, 첫 미션 시작 일시)을 반환한다.
                    - `joinedMemberCount` 는 현재 참여(JOINED) 중인 인원 수이다.
                    - 지역이 확정된 여행이면 지역 정보(`tripRegionId`, `tripRegionName`, `tripRegionImageUrl`)가 채워지고,
                      아직 지역을 선택하지 않았으면 셋 다 null 이다.
                    - 초대 코드는 이 응답에 포함되지 않으며, 별도 API(`GET /api/v1/trips/{tripId}/invite-code`)로 조회한다.
                    """)
    @GetMapping("/{tripId}")
    public ResponseDto<TripDetailResponse> getTrip(
            @Parameter(description = "조회할 여행 ID", example = "1") @PathVariable final Long tripId) {
        return ResponseDto.ok(tripService.getTrip(tripId));
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
