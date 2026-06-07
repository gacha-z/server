package dk.gatchaz.server.trip.service;

import dk.gatchaz.server.exception.CommonException;
import dk.gatchaz.server.exception.ErrorCode;
import dk.gatchaz.server.trip.dto.TripRegionDto;
import dk.gatchaz.server.trip.mapper.TripMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {

    private static final int RANDOM_REGION_COUNT = 3;

    private final TripMapper tripMapper;

    /**
     * 해당 여행(tripId)에서 아직 선택되지 않은 랜덤 지역 3개를 조회하고,
     * trip_candidate 에 후보(selected_yn = 'N', reroll_count = 1)로 저장한다.
     */
    @Transactional
    public List<TripRegionDto> getRandomRegions(final Long tripId) {
        List<TripRegionDto> regions = tripMapper.selectRandomRegions(RANDOM_REGION_COUNT, tripId);
        if (!regions.isEmpty()) {
            tripMapper.insertTripCandidates(regions, tripId);
        }
        return regions;
    }

    /**
     * 특정 후보(tripCandidateId)를 리롤한다.
     * 기존 후보 행은 비활성화(use_yn 'Y' → 'N', 이력으로 보관)하고,
     * 그 여행에서 한 번이라도 등장한 지역과 중복되지 않는 새 지역을 활성 후보로 새로 저장한다.
     * 새 후보의 reroll_count 는 직전 후보의 남은 횟수에서 1 감소하므로, 횟수가 0이 되면 더는 리롤할 수 없다.
     */
    @Transactional
    public TripRegionDto rerollRegion(final Long tripId, final Long tripCandidateId) {
        // 1. 현재 활성 후보의 남은 리롤 횟수 확인 (없거나 0 이면 리롤 불가)
        Integer remaining = tripMapper.selectRerollCount(tripCandidateId, tripId);
        if (remaining == null || remaining <= 0) {
            throw new CommonException(ErrorCode.REROLL_NOT_AVAILABLE);
        }

        // 2. 기존 후보 비활성화 (이력으로 보관)
        tripMapper.deactivateTripCandidate(tripCandidateId, tripId);

        // 3. 이력(비활성화된 지역 포함)과 중복되지 않는 새 지역 조회
        TripRegionDto newRegion = tripMapper.selectRerollRegion(tripId);
        if (newRegion == null) {
            throw new CommonException(ErrorCode.NO_AVAILABLE_TRIP_REGION);
        }

        // 4. 새 지역을 활성 후보로 저장 (남은 횟수 -1)
        tripMapper.insertRerolledCandidate(tripId, newRegion.getTripRegionId(), remaining - 1);

        return newRegion;
    }
}
