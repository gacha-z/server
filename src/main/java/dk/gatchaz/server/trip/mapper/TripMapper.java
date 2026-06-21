package dk.gatchaz.server.trip.mapper;

import dk.gatchaz.server.trip.dto.TripCreateParam;
import dk.gatchaz.server.trip.dto.TripRegionDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TripMapper {

    /**
     * 화면 입력값과 생성자(owner), 상태로 trip 을 새로 생성한다. (trip_region_id 는 지역 선택 전이므로 NULL)
     * 생성된 trip_id 는 param.tripId 에 채워진다.
     */
    int insertTrip(TripCreateParam param);

    /**
     * 사용자가 최종 선택한 지역(trip_region_id)을 trip 에 반영한다.
     * 아직 지역이 선택되지 않은(trip_region_id IS NULL) trip 만 갱신하며, 대상이 없으면 0 을 반환한다.
     */
    int updateTripRegion(@Param("tripId") Long tripId, @Param("tripRegionId") Long tripRegionId);

    /**
     * 최종 선택한 지역의 활성 후보(use_yn='Y')를 selected_yn='Y'로 확정한다.
     * 해당 지역이 활성 후보로 존재하지 않으면 0 을 반환한다.
     */
    int markCandidateSelected(@Param("tripId") Long tripId, @Param("tripRegionId") Long tripRegionId);

    /**
     * 여행 생성자(owner)를 해당 여행(tripId)의 참여자(member_rel_trip)로 등록한다. (role = 'OWNER')
     */
    int insertOwnerMember(@Param("tripId") Long tripId, @Param("memberId") Long memberId);

    /**
     * 사용 가능한(use_yn = 'Y') 여행 지역 중, 해당 여행(tripId)에서 아직 선택(selected_yn = 'Y')되지 않은
     * 지역을 무작위로 cnt 개수만큼 조회한다.
     */
    List<TripRegionDto> selectRandomRegions(@Param("cnt") int cnt, @Param("tripId") Long tripId);

    /**
     * 추천된 지역 목록을 해당 여행(tripId)의 후보로 trip_candidate 에 저장한다. (selected_yn = 'N', reroll_count = 1)
     */
    int insertTripCandidates(@Param("regions") List<TripRegionDto> regions, @Param("tripId") Long tripId);

    /**
     * 리롤 시 교체할 지역을 무작위로 1개 조회한다.
     * 해당 여행(tripId)에 아직 지역이 확정(trip.trip_region_id)되지 않았고,
     * 그 여행에서 한 번이라도 후보로 등장한 지역(현재 활성 + 리롤로 버려진 이력)은 모두 제외(중복 방지)한다.
     */
    TripRegionDto selectRerollRegion(@Param("tripId") Long tripId);

    /**
     * 현재 활성(use_yn = 'Y') 후보의 남은 리롤 횟수(reroll_count)를 조회한다.
     * 활성 후보가 없으면(없는 후보거나 이미 교체된 이력) null 을 반환한다.
     */
    Integer selectRerollCount(@Param("tripCandidateId") Long tripCandidateId, @Param("tripId") Long tripId);

    /**
     * 리롤 대상 후보(tripCandidateId)를 비활성화한다. (use_yn 'Y' → 'N', 이력으로 남김)
     * 아직 선택되지 않았고(selected_yn = 'N') 현재 활성(use_yn = 'Y')이며 리롤 가능(reroll_count > 0)일 때만 갱신된다.
     */
    int deactivateTripCandidate(@Param("tripCandidateId") Long tripCandidateId, @Param("tripId") Long tripId);

    /**
     * 리롤로 새로 추천된 지역을 해당 여행(tripId)의 활성 후보(selected_yn = 'N', use_yn = 'Y')로 저장한다.
     * reroll_count 에는 직전 후보의 남은 횟수에서 1 감소한 값을 넣는다.
     */
    int insertRerolledCandidate(@Param("tripId") Long tripId,
                                @Param("tripRegionId") Long tripRegionId,
                                @Param("rerollCount") int rerollCount);
}
