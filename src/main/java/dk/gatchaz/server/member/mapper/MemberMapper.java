package dk.gatchaz.server.member.mapper;

import dk.gatchaz.server.member.dto.MemberCreateParam;
import dk.gatchaz.server.member.dto.MemberDetailResponse;
import dk.gatchaz.server.member.dto.MemberUpdateParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MemberMapper {

    /**
     * 화면 입력값(닉네임, 나이)으로 회원을 새로 생성한다. 생성된 member_id 는 param.memberId 에 채워진다.
     */
    int insertMember(MemberCreateParam param);

    /**
     * 회원(memberId) 단건의 정보를 조회한다. 탈퇴한 회원은 조회되지 않는다. 없으면 null.
     */
    MemberDetailResponse selectMember(@Param("memberId") Long memberId);

    /**
     * 회원(memberId)의 정보(닉네임/나이)를 부분 수정한다. null 인 필드는 기존 값을 유지한다.
     * 탈퇴한 회원은 수정할 수 없으며, 대상이 없으면 0 을 반환한다.
     */
    int updateMember(MemberUpdateParam param);

    /**
     * 회원(memberId)을 탈퇴 처리한다. (deleted_yn 0 → 1, deleted_at 기록)
     * 이미 탈퇴한 회원이거나 대상이 없으면 0 을 반환한다.
     */
    int softDeleteMember(@Param("memberId") Long memberId);
}
