package dk.gatchaz.server.member.service;

import dk.gatchaz.server.exception.CommonException;
import dk.gatchaz.server.exception.ErrorCode;
import dk.gatchaz.server.member.dto.MemberCreateParam;
import dk.gatchaz.server.member.dto.MemberCreateRequest;
import dk.gatchaz.server.member.dto.MemberCreateResponse;
import dk.gatchaz.server.member.dto.MemberDetailResponse;
import dk.gatchaz.server.member.dto.MemberUpdateParam;
import dk.gatchaz.server.member.dto.MemberUpdateRequest;
import dk.gatchaz.server.member.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberMapper memberMapper;

    /**
     * 화면 입력값(닉네임, 나이)으로 회원을 생성한다.
     */
    @Transactional
    public MemberCreateResponse createMember(final MemberCreateRequest request) {
        // TODO: 소셜 로그인 연동 후, 소셜 계정(social_account)과 연결하는 흐름으로 교체.
        final MemberCreateParam param = MemberCreateParam.builder()
                .nickname(request.getNickname())
                .age(request.getAge())
                .build();

        memberMapper.insertMember(param);

        return new MemberCreateResponse(param.getMemberId());
    }

    /**
     * 회원(memberId)의 정보를 조회한다. 탈퇴했거나 없으면 예외를 던진다.
     */
    @Transactional(readOnly = true)
    public MemberDetailResponse getMember(final Long memberId) {
        // TODO: 로그인 연동 후 인증된 사용자(member_id)로 교체. 로그인 연동 전까지는 요청으로 회원 ID 를 받는다.
        final MemberDetailResponse member = memberMapper.selectMember(memberId);
        if (member == null) {
            throw new CommonException(ErrorCode.NOT_FOUND_USER);
        }
        return member;
    }

    /**
     * 회원(memberId)의 정보(닉네임/나이)를 부분 수정하고, 수정된 정보를 반환한다.
     * 대상이 없거나 탈퇴한 회원이면 예외를 던진다.
     */
    @Transactional
    public MemberDetailResponse updateMember(final Long memberId, final MemberUpdateRequest request) {
        // TODO: 로그인 연동 후 인증된 사용자(member_id)로 교체. 로그인 연동 전까지는 요청으로 회원 ID 를 받는다.
        if (request.getNickname() != null && request.getNickname().isBlank()) {
            throw new CommonException(ErrorCode.INVALID_ARGUMENT);
        }

        final MemberUpdateParam param = MemberUpdateParam.builder()
                .memberId(memberId)
                .nickname(request.getNickname())
                .age(request.getAge())
                .build();

        final int updated = memberMapper.updateMember(param);
        if (updated == 0) {
            throw new CommonException(ErrorCode.NOT_FOUND_USER);
        }

        return memberMapper.selectMember(memberId);
    }

    /**
     * 회원(memberId)을 탈퇴 처리한다(소프트 삭제). 대상이 없거나 이미 탈퇴한 회원이면 예외를 던진다.
     */
    @Transactional
    public void deleteMember(final Long memberId) {
        // TODO: 로그인 연동 후 인증된 사용자(member_id)로 교체. 로그인 연동 전까지는 요청으로 회원 ID 를 받는다.
        // TODO: 관련 데이터(여행 참여 이력, 도감 등) 삭제/익명화 정책 확정 후 반영 (현재 기획상 미확정 — 회원 소프트 삭제만 처리)
        final int deleted = memberMapper.softDeleteMember(memberId);
        if (deleted == 0) {
            throw new CommonException(ErrorCode.NOT_FOUND_USER);
        }
    }
}
