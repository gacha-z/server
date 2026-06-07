package dk.gatchaz.server.trip.controller;

import dk.gatchaz.server.dto.ResponseDto;
import dk.gatchaz.server.trip.dto.TripRegionDto;
import dk.gatchaz.server.trip.service.TripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    @Operation(summary = "랜덤 여행 지역 3개 추천", description = "use_yn = 'Y' 인 여행 지역 중 무작위로 3개를 조회한다.")
    @GetMapping("/regions/random")
    public ResponseDto<List<TripRegionDto>> getRandomRegions() {
        return ResponseDto.ok(tripService.getRandomRegions());
    }
}
