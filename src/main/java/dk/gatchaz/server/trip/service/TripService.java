package dk.gatchaz.server.trip.service;

import dk.gatchaz.server.common.exception.CommonException;
import dk.gatchaz.server.common.exception.ErrorCode;
import dk.gatchaz.server.notification.event.TripCancelledEvent;
import dk.gatchaz.server.trip.dto.TripCreateParam;
import dk.gatchaz.server.trip.dto.TripCreateRequest;
import dk.gatchaz.server.trip.dto.TripCreateResponse;
import dk.gatchaz.server.trip.dto.TripDetailResponse;
import dk.gatchaz.server.trip.dto.TripInviteCodeResponse;
import dk.gatchaz.server.trip.dto.TripJoinInfo;
import dk.gatchaz.server.trip.dto.TripJoinResponse;
import dk.gatchaz.server.trip.dto.TripListResponse;
import dk.gatchaz.server.trip.dto.TripMemberResponse;
import dk.gatchaz.server.trip.dto.TripRegionDto;
import dk.gatchaz.server.trip.dto.TripSearchParam;
import dk.gatchaz.server.trip.dto.TripSearchRequest;
import dk.gatchaz.server.trip.dto.TripSummaryResponse;
import dk.gatchaz.server.trip.dto.TripUpdateParam;
import dk.gatchaz.server.trip.dto.TripUpdateRequest;
import dk.gatchaz.server.trip.mapper.TripMapper;
import dk.gatchaz.server.trip.support.InviteCodeGenerator;
import dk.gatchaz.server.type.ETripMemberRole;
import dk.gatchaz.server.type.ETripStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {

    private static final int RANDOM_REGION_COUNT = 3;

    private final TripMapper tripMapper;
    private final InviteCodeGenerator inviteCodeGenerator;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 화면 입력값으로 여행을 생성한다. 지역은 이 단계에서 선택하지 않으므로 trip_region_id 는 비워둔다.
     * 이후 추천/리롤로 후보를 받고, 지역 선택 단계(selectTripRegion)에서 trip_region_id 를 확정한다.
     * 하나의 트랜잭션으로 처리되어, 아래 중 하나라도 실패하면 전체 롤백된다.
     * 1. 화면 입력값과 생성자(owner), 상태, 초대 코드를 trip 에 INSERT 한다.
     * 2. 생성자(owner)를 member_rel_trip 에 OWNER 로 등록한다.
     */
    @Transactional
    public TripCreateResponse createTrip(final TripCreateRequest request, final Long userId) {
        // userId 는 컨트롤러에서 @UserId 로 주입된 인증된 사용자 ID 이다. 이 여행의 생성자(owner)가 된다.

        // 첫 미션 시각(시·분)을 여행 시작일과 합쳐 저장용 일시로 가공
        final LocalDateTime missionStartAt = LocalDateTime.of(request.getStartDate(), request.getMissionStartTime());

        // 1. 화면 입력값으로 trip 생성 (지역 미선택 상태, trip_region_id IS NULL / 초대 코드 영구 발급)
        final TripCreateParam param = TripCreateParam.builder()
                .ownerMemberId(userId)
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
        tripMapper.insertTripMember(tripId, userId, ETripMemberRole.OWNER.name());

        return new TripCreateResponse(tripId);
    }

    /**
     * 로그인한 회원이 참여(JOINED)한 여행을 검색 조건(이름/지역/기간/상태)으로 필터링하여 조회한다.
     * 커서 기반 무한 스크롤로, hasNext 판별을 위해 요청 개수 + 1 을 조회한 뒤 초과분을 잘라낸다.
     */
    @Transactional(readOnly = true)
    public TripListResponse getTrips(final TripSearchRequest request) {
        // userId 는 컨트롤러에서 @UserId 로 주입된 인증된 사용자 ID 이다.
        final Long userId = request.getMemberId();

        // size 범위 보정 (1~50)
        final int size = Math.min(Math.max(request.getSize(), 1), 50);

        final TripSearchParam param = TripSearchParam.builder()
                .memberId(userId)
                .title(request.getTitle())
                .tripRegionId(request.getTripRegionId())
                .status(request.getStatus() == null ? null : request.getStatus().name())
                .dateFrom(request.getDateFrom())
                .dateTo(request.getDateTo())
                .cursor(request.getCursor())
                .size(size + 1) // 다음 페이지 존재 여부 판별용으로 1개 더 조회
                .build();

        final List<TripSummaryResponse> trips = tripMapper.selectTrips(param);

        final boolean hasNext = trips.size() > size;
        final List<TripSummaryResponse> pageTrips = hasNext ? trips.subList(0, size) : trips;
        final Long nextCursor = hasNext ? pageTrips.get(pageTrips.size() - 1).getTripId() : null;

        return new TripListResponse(pageTrips, nextCursor, hasNext);
    }

    /**
     * 여행(tripId) 단건의 상세 정보를 조회한다. 없으면 404(NOT_FOUND_TRIP),
     * 요청자가 그 여행 참여자가 아니면 404(NOT_FOUND_TRIP_MEMBER)를 던진다.
     */
    @Transactional(readOnly = true)
    public TripDetailResponse getTrip(final Long tripId, final Long userId) {
        final TripDetailResponse trip = tripMapper.selectTrip(tripId);
        if (trip == null) {
            throw new CommonException(ErrorCode.NOT_FOUND_TRIP);
        }
        requireTripMember(tripId, userId);
        return trip;
    }

    /**
     * 방장(여행 생성자)이 여행(tripId)의 기본 정보를 부분 수정하고, 수정된 상세 정보를 반환한다.
     * 요청에 포함된(null 이 아닌) 필드만 변경하고 나머지는 기존 값을 유지한다.
     * 1. 여행 존재 여부와 요청자가 방장인지 확인한다.
     * 2. 제목은 보낸 경우 공백일 수 없고, 정원은 현재 참여 인원보다 작게 줄일 수 없다.
     * 3. 시작일 또는 미션 시작 시각이 바뀌면 첫 미션 일시를 "최종 시작일 + 최종 시각"으로 재계산한다.
     */
    @Transactional
    public TripDetailResponse updateTrip(final Long tripId, final TripUpdateRequest request, final Long userId) {
        // userId 는 컨트롤러에서 @UserId 로 주입된 인증된 사용자 ID 이다.

        // 1. 현재 여행 조회 (존재 확인 + 방장 확인 + 미변경 필드의 기존 값 확보)
        final TripDetailResponse current = tripMapper.selectTrip(tripId);
        if (current == null) {
            throw new CommonException(ErrorCode.NOT_FOUND_TRIP);
        }
        if (!current.getOwnerMemberId().equals(userId)) {
            throw new CommonException(ErrorCode.NOT_TRIP_OWNER);
        }

        // 2. 보낸 필드만 값 검증
        if (request.getTitle() != null && request.getTitle().isBlank()) {
            throw new CommonException(ErrorCode.INVALID_ARGUMENT);
        }
        if (request.getMemberLimit() != null && request.getMemberLimit() < current.getJoinedMemberCount()) {
            throw new CommonException(ErrorCode.TRIP_MEMBER_LIMIT_BELOW_JOINED);
        }

        // 수정할 필드가 하나도 없으면 변경 없이 현재 상세 정보를 반환
        final boolean hasUpdates = request.getTitle() != null || request.getStartDate() != null
                || request.getEndDate() != null || request.getMemberLimit() != null
                || request.getMissionMin() != null || request.getMissionMax() != null
                || request.getMissionStartTime() != null;
        if (!hasUpdates) {
            return current;
        }

        // 3. 시작일 또는 미션 시작 시각이 바뀌면 첫 미션 일시 재계산 (바뀌지 않은 쪽은 기존 값 사용)
        LocalDateTime missionStartAt = null;
        if (request.getStartDate() != null || request.getMissionStartTime() != null) {
            final LocalDate startDate =
                    request.getStartDate() != null ? request.getStartDate() : current.getStartDate();
            final LocalTime missionStartTime = request.getMissionStartTime() != null
                    ? request.getMissionStartTime()
                    : (current.getMissionStartAt() != null ? current.getMissionStartAt().toLocalTime() : null);
            if (missionStartTime != null) {
                missionStartAt = LocalDateTime.of(startDate, missionStartTime);
            }
        }

        final TripUpdateParam param = TripUpdateParam.builder()
                .tripId(tripId)
                .title(request.getTitle())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .memberLimit(request.getMemberLimit())
                .missionMin(request.getMissionMin())
                .missionMax(request.getMissionMax())
                .missionStartAt(missionStartAt)
                .build();

        tripMapper.updateTrip(param);

        return tripMapper.selectTrip(tripId);
    }

    /**
     * 여행(tripId)에 참여(JOINED) 중인 팀원 목록을 참여 등록 순으로 조회한다. 여행이 없으면 404(NOT_FOUND_TRIP),
     * 요청자가 그 여행 참여자가 아니면 404(NOT_FOUND_TRIP_MEMBER)를 던진다.
     */
    @Transactional(readOnly = true)
    public List<TripMemberResponse> getTripMembers(final Long tripId, final Long userId) {
        if (tripMapper.existsTrip(tripId) == 0) {
            throw new CommonException(ErrorCode.NOT_FOUND_TRIP);
        }
        requireTripMember(tripId, userId);
        return tripMapper.selectTripMembers(tripId);
    }

    /**
     * 방장(여행 생성자)이 팀원(memberId)을 여행(tripId)에서 강퇴한다.
     * 1. 여행 존재 여부와 요청자가 방장인지 확인한다.
     * 2. 방장 자신은 강퇴할 수 없다.
     * 3. 참여(JOINED) 중인 팀원이면 강퇴 처리한다. (status 'JOINED' → 'KICKED', left_at 기록)
     */
    @Transactional
    public void kickTripMember(final Long tripId, final Long memberId, final Long userId) {
        // userId 는 컨트롤러에서 @UserId 로 주입된 인증된 사용자 ID 이다. memberId 는 강퇴 대상이다.

        // 1. 여행 존재 + 방장 확인
        final Long ownerMemberId = tripMapper.selectTripOwnerMemberId(tripId);
        if (ownerMemberId == null) {
            throw new CommonException(ErrorCode.NOT_FOUND_TRIP);
        }
        if (!ownerMemberId.equals(userId)) {
            throw new CommonException(ErrorCode.NOT_TRIP_OWNER);
        }

        // 2. 방장 자신은 강퇴 불가
        if (ownerMemberId.equals(memberId)) {
            throw new CommonException(ErrorCode.CANNOT_KICK_TRIP_OWNER);
        }

        // 3. 참여 중인 팀원 강퇴 (대상이 JOINED 상태가 아니면 실패)
        final int kicked = tripMapper.kickTripMember(tripId, memberId);
        if (kicked == 0) {
            throw new CommonException(ErrorCode.NOT_FOUND_TRIP_MEMBER);
        }
    }

    /**
     * 팀원(memberId)이 여행(tripId)에서 스스로 나간다. 하나의 트랜잭션으로 처리된다.
     * 1. 나가려는 사람이 방장인데 마지막 1명이면 나갈 수 없다. (여행 취소 API 를 이용해야 한다)
     * 2. 참여(JOINED) 상태를 LEFT 로 변경하고 left_at 을 기록한다.
     * 3. 나간 사람이 방장이면 남은 팀원 중 1명에게 무작위로 방장을 위임한다.
     *    (member_rel_trip.role → OWNER, trip.owner_member_id 변경)
     */
    @Transactional
    public void leaveTrip(final Long tripId, final Long userId) {
        // userId 는 컨트롤러에서 @UserId 로 주입된 인증된 사용자 ID 이다.

        final Long ownerMemberId = tripMapper.selectTripOwnerMemberId(tripId);
        if (ownerMemberId == null) {
            throw new CommonException(ErrorCode.NOT_FOUND_TRIP);
        }

        final boolean isOwner = ownerMemberId.equals(userId);

        // 1. 방장이 마지막 1명이면 나가기 차단
        if (isOwner && tripMapper.countJoinedMembers(tripId) <= 1) {
            throw new CommonException(ErrorCode.LAST_TRIP_MEMBER_CANNOT_LEAVE);
        }

        // 2. 자진 탈퇴 처리 (참여 중이 아니면 실패)
        final int left = tripMapper.leaveTripMember(tripId, userId);
        if (left == 0) {
            throw new CommonException(ErrorCode.NOT_FOUND_TRIP_MEMBER);
        }

        // 3. 방장이 나가면 남은 팀원 중 무작위 1명에게 방장 위임
        if (isOwner) {
            final Long newOwnerMemberId = tripMapper.selectRandomJoinedMemberId(tripId);
            if (newOwnerMemberId == null) {
                // 동시에 다른 팀원이 나가 남은 인원이 없어진 경우. 전체 롤백되어 나가기도 취소된다.
                throw new CommonException(ErrorCode.LAST_TRIP_MEMBER_CANNOT_LEAVE);
            }
            tripMapper.updateTripMemberRole(tripId, newOwnerMemberId, ETripMemberRole.OWNER.name());
            tripMapper.updateTripOwner(tripId, newOwnerMemberId);
        }
    }

    /**
     * 방장(여행 생성자)이 지정한 팀원(newOwnerMemberId)에게 방장을 위임한다. 하나의 트랜잭션으로 처리된다.
     * 1. 여행 존재 여부와 요청자가 방장인지 확인한다.
     * 2. 위임 대상이 현재 방장 자신이면 실패한다.
     * 3. 위임 대상을 OWNER 로 승격한다. (참여 중인 팀원이 아니면 실패)
     * 4. 팀에 남는 기존 방장을 MEMBER 로 변경하고, trip 의 owner_member_id 를 새 방장으로 변경한다.
     */
    @Transactional
    public void transferTripOwner(final Long tripId, final Long newOwnerMemberId, final Long userId) {
        // userId 는 컨트롤러에서 @UserId 로 주입된 인증된 사용자 ID 이다.

        // 1. 여행 존재 + 방장 확인
        final Long ownerMemberId = tripMapper.selectTripOwnerMemberId(tripId);
        if (ownerMemberId == null) {
            throw new CommonException(ErrorCode.NOT_FOUND_TRIP);
        }
        if (!ownerMemberId.equals(userId)) {
            throw new CommonException(ErrorCode.NOT_TRIP_OWNER);
        }

        // 2. 자기 자신에게는 위임 불가
        if (ownerMemberId.equals(newOwnerMemberId)) {
            throw new CommonException(ErrorCode.ALREADY_TRIP_OWNER);
        }

        // 3. 위임 대상 승격 (참여 중인 팀원이 아니면 실패)
        final int promoted = tripMapper.updateTripMemberRole(tripId, newOwnerMemberId, ETripMemberRole.OWNER.name());
        if (promoted == 0) {
            throw new CommonException(ErrorCode.NOT_FOUND_TRIP_MEMBER);
        }

        // 4. 기존 방장은 일반 팀원으로 변경하고 trip 에 새 방장 반영
        tripMapper.updateTripMemberRole(tripId, ownerMemberId, ETripMemberRole.MEMBER.name());
        tripMapper.updateTripOwner(tripId, newOwnerMemberId);
    }

    /**
     * 방장(여행 생성자)이 여행(tripId)을 취소한다. (status 'CREATED' → 'CANCELLED')
     * 마지막 1명 남은 방장이 여행을 정리할 때 사용한다. 팀원 참여 이력은 그대로 보존된다.
     */
    @Transactional
    public void cancelTrip(final Long tripId, final Long userId) {
        // userId 는 컨트롤러에서 @UserId 로 주입된 인증된 사용자 ID 이다.

        // 1. 여행 존재 + 방장 확인
        final Long ownerMemberId = tripMapper.selectTripOwnerMemberId(tripId);
        if (ownerMemberId == null) {
            throw new CommonException(ErrorCode.NOT_FOUND_TRIP);
        }
        if (!ownerMemberId.equals(userId)) {
            throw new CommonException(ErrorCode.NOT_TRIP_OWNER);
        }

        // 2. 취소 처리 (생성(CREATED) 상태가 아니면 실패)
        final int cancelled = tripMapper.cancelTrip(tripId);
        if (cancelled == 0) {
            throw new CommonException(ErrorCode.TRIP_NOT_CANCELLABLE);
        }

        // 여행 기록/미션 수행 이력은 그대로 남기되, 이 여행에서 오른 배지 진행도는 되돌려야 한다.
        // (완료 때만 지급되는 여행 횟수/지역별/지역 탐험 배지·아이템은 취소로는 지급된 적이 없어 되돌릴 게 없다)
        eventPublisher.publishEvent(new TripCancelledEvent(tripId));
    }

    /**
     * 요청자(userId)가 해당 여행(tripId)에 참여(JOINED) 중인지 확인한다. 아니면 예외를 던진다.
     */
    private void requireTripMember(final Long tripId, final Long userId) {
        if (tripMapper.existsTripMember(tripId, userId) == 0) {
            throw new CommonException(ErrorCode.NOT_FOUND_TRIP_MEMBER);
        }
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
     * 방장(여행 생성자)이 해당 여행(tripId)의 초대 코드를 조회한다. 코드는 생성 시 발급되어 만료되지 않는다.
     * (프론트에서 도메인을 붙여 링크로 사용) 여행이 없으면 404, 요청자가 방장이 아니면 403 을 던진다.
     */
    @Transactional(readOnly = true)
    public TripInviteCodeResponse getInviteCode(final Long tripId, final Long userId) {
        final Long ownerMemberId = tripMapper.selectTripOwnerMemberId(tripId);
        if (ownerMemberId == null) {
            throw new CommonException(ErrorCode.NOT_FOUND_TRIP);
        }
        if (!ownerMemberId.equals(userId)) {
            throw new CommonException(ErrorCode.NOT_TRIP_OWNER);
        }
        return new TripInviteCodeResponse(tripMapper.selectInviteCode(tripId));
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
    public TripJoinResponse joinTrip(final String code, final Long userId) {
        // 1. 초대 코드로 여행 조회
        final TripJoinInfo trip = tripMapper.selectTripByInviteCode(code);
        if (trip == null) {
            throw new CommonException(ErrorCode.INVALID_INVITE_CODE);
        }

        // 2. 참여하려는 회원이 실제로 존재하는지 확인
        if (tripMapper.existsMember(userId) == 0) {
            throw new CommonException(ErrorCode.NOT_FOUND_USER);
        }

        // 3. 참여 가능한 상태인지 확인 (생성 상태에서만 참여 가능)
        if (!ETripStatus.CREATED.name().equals(trip.getStatus())) {
            throw new CommonException(ErrorCode.TRIP_NOT_JOINABLE);
        }

        // 4. 현재 참여(JOINED) 중인 회원인지 확인 (나갔거나 강퇴된 회원은 재참여 가능, 새 참여 이력이 생성된다)
        if (tripMapper.existsTripMember(trip.getTripId(), userId) > 0) {
            throw new CommonException(ErrorCode.ALREADY_JOINED_TRIP);
        }

        // 5. 정원 확인
        if (tripMapper.countJoinedMembers(trip.getTripId()) >= trip.getMemberLimit()) {
            throw new CommonException(ErrorCode.TRIP_FULL);
        }

        // 6. 참여자로 등록
        tripMapper.insertTripMember(trip.getTripId(), userId, ETripMemberRole.MEMBER.name());

        return new TripJoinResponse(trip.getTripId());
    }

    /**
     * 사용자가 최종 선택한 지역을 여행에 반영한다. 하나의 트랜잭션으로 처리된다.
     * 요청자가 그 여행 참여자가 아니면 예외를 던진다.
     * 1. 선택한 지역을 trip_candidate 에서 selected_yn='Y'로 확정한다. (활성 후보가 아니면 실패)
     * 2. 선택한 지역(trip_region_id)을 trip 에 반영한다. (지역 미선택 상태의 trip 이 없으면 실패)
     */
    @Transactional
    public TripCreateResponse selectTripRegion(final Long tripId, final Long tripRegionId, final Long userId) {
        requireTripMember(tripId, userId);

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
     * 요청자가 그 여행 참여자가 아니면 예외를 던진다.
     */
    @Transactional
    public List<TripRegionDto> getRandomRegions(final Long tripId, final Long userId) {
        requireTripMember(tripId, userId);

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
     * 요청자가 그 여행 참여자가 아니면 예외를 던진다.
     */
    @Transactional
    public TripRegionDto rerollRegion(final Long tripId, final Long tripCandidateId, final Long userId) {
        requireTripMember(tripId, userId);

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

        // 4. 새 지역을 활성 후보로 저장 (남은 횟수 -1). 생성된 trip_candidate_id 가 newRegion 에 채워진다.
        tripMapper.insertRerolledCandidate(newRegion, tripId, remaining - 1);

        return newRegion;
    }
}
