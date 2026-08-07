package dk.gatchaz.server.trip.mapper;

import dk.gatchaz.server.trip.dto.TripCreateParam;
import dk.gatchaz.server.trip.dto.TripDetailResponse;
import dk.gatchaz.server.trip.dto.TripJoinInfo;
import dk.gatchaz.server.trip.dto.TripMemberResponse;
import dk.gatchaz.server.trip.dto.TripRegionDto;
import dk.gatchaz.server.trip.dto.TripSearchParam;
import dk.gatchaz.server.trip.dto.TripSummaryResponse;
import dk.gatchaz.server.trip.dto.TripUpdateParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TripMapper {

    /**
     * 회원(memberId)이 참여(JOINED)한 여행을, 검색 조건(이름/지역/기간/상태)으로 필터링하여 조회한다.
     * 커서(cursor)보다 이전 여행(trip_id 내림차순)만 size 개수만큼 조회한다.
     * hasNext 판별을 위해 서비스에서 요청 개수 + 1 을 size 로 넘긴다.
     */
    List<TripSummaryResponse> selectTrips(TripSearchParam param);

    /**
     * 여행(tripId) 단건의 상세 정보(기본 정보 + 미션 설정 + 지역 + 참여 인원 수)를 조회한다. 없으면 null.
     */
    TripDetailResponse selectTrip(@Param("tripId") Long tripId);

    /**
     * 여행(tripId)이 실제로 존재하는지 확인한다. (존재하면 1 이상)
     */
    int existsTrip(@Param("tripId") Long tripId);

    /**
     * 여행(tripId)에 참여(JOINED) 중인 팀원 목록을 참여 등록 순(member_rel_id 오름차순)으로 조회한다.
     * 생성자(OWNER)가 여행 생성 시 가장 먼저 등록되므로 목록의 처음에 온다.
     */
    List<TripMemberResponse> selectTripMembers(@Param("tripId") Long tripId);

    /**
     * 여행(tripId)의 생성자(owner) 회원 ID를 조회한다. trip 이 없으면 null.
     */
    Long selectTripOwnerMemberId(@Param("tripId") Long tripId);

    /**
     * 여행(tripId)에 참여(JOINED) 중인 팀원(memberId)을 강퇴 처리한다. (status 'JOINED' → 'KICKED', left_at 기록)
     * 참여 중인 팀원이 아니면 0 을 반환한다.
     */
    int kickTripMember(@Param("tripId") Long tripId, @Param("memberId") Long memberId);

    /**
     * 여행(tripId)에 참여(JOINED) 중인 팀원(memberId)을 자진 탈퇴 처리한다. (status 'JOINED' → 'LEFT', left_at 기록)
     * 참여 중인 팀원이 아니면 0 을 반환한다.
     */
    int leaveTripMember(@Param("tripId") Long tripId, @Param("memberId") Long memberId);

    /**
     * 여행(tripId)에 참여(JOINED) 중인 팀원 중 1명을 무작위로 조회한다. (방장 위임용) 없으면 null.
     * 대상이 여행 정원(member_limit) 이내의 소수이므로 ORDER BY RAND() 비용은 무시할 수준이다.
     */
    Long selectRandomJoinedMemberId(@Param("tripId") Long tripId);

    /**
     * 여행(tripId)에 참여(JOINED) 중인 팀원(memberId)의 역할을 변경한다.
     * (방장 위임 시 새 방장은 OWNER 로 승격, 팀에 남는 기존 방장은 MEMBER 로 변경)
     * 참여 중인 팀원이 아니면 0 을 반환한다.
     */
    int updateTripMemberRole(@Param("tripId") Long tripId,
                             @Param("memberId") Long memberId,
                             @Param("role") String role);

    /**
     * 여행(tripId)의 생성자(owner_member_id)를 새 방장(ownerMemberId)으로 변경한다.
     */
    int updateTripOwner(@Param("tripId") Long tripId, @Param("ownerMemberId") Long ownerMemberId);

    /**
     * 여행(tripId)을 취소 처리한다. (status 'CREATED' → 'CANCELLED')
     * 생성(CREATED) 상태가 아니면 0 을 반환한다.
     */
    int cancelTrip(@Param("tripId") Long tripId);

    /**
     * 여행(tripId)의 기본 정보(제목/기간/정원/미션 설정)를 부분 수정한다. null 인 필드는 기존 값을 유지한다.
     * 지역(trip_region_id), 상태(status), 방장(owner_member_id), 초대 코드는 각 전용 API 에서만 변경한다.
     */
    int updateTrip(TripUpdateParam param);

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
     * 회원을 해당 여행(tripId)의 참여자(member_rel_trip)로 등록한다. (status = 'JOINED')
     */
    int insertTripMember(@Param("tripId") Long tripId,
                         @Param("memberId") Long memberId,
                         @Param("role") String role);

    /**
     * 초대 코드의 중복 여부를 확인한다. (이미 존재하면 1 이상)
     */
    int existsInviteCode(@Param("code") String code);

    /**
     * 해당 여행(tripId)의 초대 코드를 조회한다. trip 이 없으면 null.
     */
    String selectInviteCode(@Param("tripId") Long tripId);

    /**
     * 초대 코드로 여행을 조회한다. (참여 검증용 최소 정보) 코드가 유효하지 않으면 null.
     */
    TripJoinInfo selectTripByInviteCode(@Param("code") String code);

    /**
     * 해당 여행(tripId)의 현재 참여 중(status='JOINED') 인원 수를 조회한다.
     */
    int countJoinedMembers(@Param("tripId") Long tripId);

    /**
     * 회원이 현재 해당 여행(tripId)에 참여(JOINED) 중인지 확인한다. (참여 중이면 1 이상)
     * 나갔거나(LEFT) 강퇴된(KICKED) 이력만 있는 회원은 0 을 반환하므로 재참여가 가능하다.
     */
    int existsTripMember(@Param("tripId") Long tripId, @Param("memberId") Long memberId);

    /**
     * 회원(memberId)이 실제로 존재하는지 확인한다. (존재하면 1 이상)
     */
    int existsMember(@Param("memberId") Long memberId);

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
