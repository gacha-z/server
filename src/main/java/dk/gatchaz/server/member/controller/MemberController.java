package dk.gatchaz.server.member.controller;

import dk.gatchaz.server.common.annotation.UserId;
import dk.gatchaz.server.common.dto.ResponseDto;
import dk.gatchaz.server.member.dto.MemberCreateRequest;
import dk.gatchaz.server.member.dto.MemberCreateResponse;
import dk.gatchaz.server.member.dto.MemberDetailResponse;
import dk.gatchaz.server.member.dto.MemberUpdateRequest;
import dk.gatchaz.server.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member", description = "회원 API")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 회원가입 기본 정보 저장 (Create → body DTO)
     */
    @Operation(summary = "회원가입 기본 정보 저장", description = "닉네임과 나이를 받아 회원을 생성한다. 소셜 로그인 연동 전까지는 이 API 로 회원을 직접 생성한다.")
    @PostMapping
    public ResponseDto<MemberCreateResponse> createMember(@Valid @RequestBody final MemberCreateRequest request) {
        return ResponseDto.created(memberService.createMember(request));
    }

    /**
     * 내 정보 조회 (Read → param)
     */
    @Operation(summary = "내 정보 조회", description = "회원 ID로 프로필 정보를 조회한다. 탈퇴한 회원이거나 없으면 404 를 반환한다.")
    @GetMapping("/me")
    public ResponseDto<MemberDetailResponse> getMe(@UserId final Long memberId) {
        return ResponseDto.ok(memberService.getMember(memberId));
    }

    /**
     * 회원정보 수정 (Update → body DTO, 대상 식별용 memberId 만 param)
     */
    @Operation(
            summary = "회원정보 수정",
            description = """
                    회원의 닉네임/나이를 부분 수정한다. body 에 포함한 필드만 변경되고, 보내지 않은 필드는 기존 값이 유지된다.
                    수정된 회원 정보를 반환하며, 탈퇴한 회원이거나 없으면 404 를 반환한다.
                    """)
    @PatchMapping("/me")
    public ResponseDto<MemberDetailResponse> updateMe(
            @UserId final Long memberId,
            @Valid @RequestBody final MemberUpdateRequest request) {
        return ResponseDto.ok(memberService.updateMember(memberId, request));
    }

    /**
     * 회원 탈퇴 (Delete → param)
     */
    @Operation(summary = "회원 탈퇴", description = "회원을 탈퇴 처리한다(소프트 삭제). 이미 탈퇴했거나 없으면 404 를 반환한다.")
    @DeleteMapping("/me")
    public ResponseDto<Void> deleteMe(@UserId final Long memberId) {
        memberService.deleteMember(memberId);
        return ResponseDto.<Void>ok(null);
    }
}
