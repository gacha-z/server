package dk.gatchaz.server.trip.controller;

import dk.gatchaz.server.dto.ResponseDto;
import dk.gatchaz.server.trip.dto.TripRegionDto;
import dk.gatchaz.server.trip.service.TripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
    public ResponseDto<TripRegionDto> rerollRegion(@RequestParam final Long tripId,
                                                   @RequestParam final Long tripCandidateId) {
        return ResponseDto.ok(tripService.rerollRegion(tripId, tripCandidateId));
    }
}
