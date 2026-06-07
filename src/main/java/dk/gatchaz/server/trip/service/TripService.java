package dk.gatchaz.server.trip.service;

import dk.gatchaz.server.trip.dto.TripRegionDto;
import dk.gatchaz.server.trip.mapper.TripMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {

    private static final int RANDOM_REGION_COUNT = 3;

    private final TripMapper tripMapper;

    /**
     * 여행 생성 시 추천할 랜덤 지역 3개를 조회한다.
     */
    public List<TripRegionDto> getRandomRegions() {
        return tripMapper.selectRandomRegions(RANDOM_REGION_COUNT);
    }
}
