package dk.gatchaz.server.trip.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 여행 참여 팀원 목록 조회 시 각 팀원의 정보.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "여행 참여 팀원 정보")
public class TripMemberResponse {

    @Schema(description = "회원 ID", example = "1")
    private Long memberId;

    @Schema(description = "닉네임. 설정하지 않았으면 null", example = "여행러버")
    private String nickname;

    @Schema(description = "프로필 이미지 URL. 설정하지 않았으면 null", example = "https://cdn.example.com/profiles/1.jpg")
    private String profileImageUrl;

    @Schema(description = "여행 내 역할 (OWNER: 여행 생성자 / MEMBER: 참여자)", example = "OWNER")
    private String role;

    @Schema(description = "여행 참여 일시", example = "2026-07-01T10:00:00")
    private LocalDateTime joinedAt;
}
