package dk.gatchaz.server.trip.service;

import dk.gatchaz.server.exception.CommonException;
import dk.gatchaz.server.exception.ErrorCode;
import dk.gatchaz.server.trip.dto.TripCreateParam;
import dk.gatchaz.server.trip.dto.TripCreateRequest;
import dk.gatchaz.server.trip.dto.TripCreateResponse;
import dk.gatchaz.server.trip.dto.TripInviteCodeResponse;
import dk.gatchaz.server.trip.dto.TripJoinInfo;
import dk.gatchaz.server.trip.dto.TripJoinResponse;
import dk.gatchaz.server.trip.dto.TripRegionDto;
import dk.gatchaz.server.trip.mapper.TripMapper;
import dk.gatchaz.server.trip.support.InviteCodeGenerator;
import dk.gatchaz.server.type.ETripMemberRole;
import dk.gatchaz.server.type.ETripStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {

    private static final int RANDOM_REGION_COUNT = 3;

    private final TripMapper tripMapper;
    private final InviteCodeGenerator inviteCodeGenerator;

    /**
     * 화면 입력값으로 여행을 생성한다. 지역은 이 단계에서 선택하지 않으므로 trip_region_id 는 비워둔다.
     * 이후 추천/리롤로 후보를 받고, 지역 선택 단계(selectTripRegion)에서 trip_region_id 를 확정한다.
     * 하나의 트랜잭션으로 처리되어, 아래 중 하나라도 실패하면 전체 롤백된다.
     * 1. 화면 입력값과 생성자(owner), 상태, 초대 코드를 trip 에 INSERT 한다.
     * 2. 생성자(owner)를 member_rel_trip 에 OWNER 로 등록한다.
     */
    @Transactional
    public TripCreateResponse createTrip(final TripCreateRequest request) {
        // TODO: 로그인 연동 후 인증된 사용자(member_id)로 교체. 현재는 임시로 1번 회원을 owner 로 사용한다.
        // final Long ownerMemberId = AuthUtil.getCurrentMemberId();
        final Long ownerMemberId = 1L;

        // 첫 미션 시각(시·분)을 여행 시작일과 합쳐 저장용 일시로 가공
        final LocalDateTime missionStartAt = LocalDateTime.of(request.getStartDate(), request.getMissionStartTime());

        // 1. 화면 입력값으로 trip 생성 (지역 미선택 상태, trip_region_id IS NULL / 초대 코드 영구 발급)
        final TripCreateParam param = TripCreateParam.builder()
                .ownerMemberId(ownerMemberId)
                .title(request.getTitle())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .memberLimit(request.getMemberLimit())
                .missionMin(request.getMissionMin())
                .missionMax(request.getMissionMax())
                .missionStartAt(missionStartAt)
                .status(ETripStatus.CREATED.name())
                .inviteCode(generateUniqueInviteCode())
                .build();

        tripMapper.insertTrip(param);
        final Long tripId = param.getTripId();

        // 2. 생성자(owner)를 참여자로 등록
        tripMapper.insertTripMember(tripId, ownerMemberId, ETripMemberRole.OWNER.name());

        return new TripCreateResponse(tripId);
    }

    /**
     * 중복되지 않는 초대 코드를 생성한다. 충돌은 사실상 발생하지 않지만, 혹시 존재하면 다시 생성한다.
     */
    private String generateUniqueInviteCode() {
        String code;
        do {
            code = inviteCodeGenerator.generate();
        } while (tripMapper.existsInviteCode(code) > 0);
        return code;
    }

    /**
     * 해당 여행(tripId)의 초대 코드를 반환한다. 코드는 생성 시 발급되어 만료되지 않는다.
     * (프론트에서 도메인을 붙여 링크로 사용)
     */
    @Transactional(readOnly = true)
    public TripInviteCodeResponse getInviteCode(final Long tripId) {
        final String code = tripMapper.selectInviteCode(tripId);
        if (code == null) {
            throw new CommonException(ErrorCode.NOT_FOUND_TRIP);
        }
        return new TripInviteCodeResponse(code);
    }

    /**
     * 초대 코드(링크)로 회원을 여행에 참여시킨다. 하나의 트랜잭션으로 처리된다.
     * 1. 코드로 여행을 조회한다. (유효하지 않으면 실패)
     * 2. 참여하려는 회원이 실제로 존재하는지 확인한다.
     * 3. 참여 가능한 상태(CREATED)인지 확인한다.
     * 4. 이미 참여한 회원인지 확인한다.
     * 5. 정원이 남아 있는지 확인한다.
     * 6. member_rel_trip 에 MEMBER 로 등록한다.
     */
    @Transactional
    public TripJoinResponse joinTrip(final String code, final Long memberId) {
        // 1. 초대 코드로 여행 조회
        final TripJoinInfo trip = tripMapper.selectTripByInviteCode(code);
        if (trip == null) {
            throw new CommonException(ErrorCode.INVALID_INVITE_CODE);
        }

        // 2. 참여하려는 회원이 실제로 존재하는지 확인
        if (tripMapper.existsMember(memberId) == 0) {
            throw new CommonException(ErrorCode.NOT_FOUND_USER);
        }

        // 3. 참여 가능한 상태인지 확인 (생성 상태에서만 참여 가능)
        if (!ETripStatus.CREATED.name().equals(trip.getStatus())) {
            throw new CommonException(ErrorCode.TRIP_NOT_JOINABLE);
        }

        // 4. 이미 참여한 회원인지 확인
        if (tripMapper.existsTripMember(trip.getTripId(), memberId) > 0) {
            throw new CommonException(ErrorCode.ALREADY_JOINED_TRIP);
        }

        // 5. 정원 확인
        if (tripMapper.countJoinedMembers(trip.getTripId()) >= trip.getMemberLimit()) {
            throw new CommonException(ErrorCode.TRIP_FULL);
        }

        // 6. 참여자로 등록
        tripMapper.insertTripMember(trip.getTripId(), memberId, ETripMemberRole.MEMBER.name());

        return new TripJoinResponse(trip.getTripId());
    }

    /**
     * 사용자가 최종 선택한 지역을 여행에 반영한다. 하나의 트랜잭션으로 처리된다.
     * 1. 선택한 지역을 trip_candidate 에서 selected_yn='Y'로 확정한다. (활성 후보가 아니면 실패)
     * 2. 선택한 지역(trip_region_id)을 trip 에 반영한다. (지역 미선택 상태의 trip 이 없으면 실패)
     */
    @Transactional
    public TripCreateResponse selectTripRegion(final Long tripId, final Long tripRegionId) {
        // 1. 선택한 지역을 후보에서 확정 (선택한 지역이 활성 후보로 존재하지 않으면 잘못된 선택)
        final int selected = tripMapper.markCandidateSelected(tripId, tripRegionId);
        if (selected == 0) {
            throw new CommonException(ErrorCode.INVALID_TRIP_REGION_SELECTION);
        }

        // 2. 선택한 지역을 trip 에 반영 (대상 trip 이 없거나 이미 지역이 선택되었으면 실패)
        final int updated = tripMapper.updateTripRegion(tripId, tripRegionId);
        if (updated == 0) {
            throw new CommonException(ErrorCode.NOT_FOUND_TRIP);
        }

        return new TripCreateResponse(tripId);
    }

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
