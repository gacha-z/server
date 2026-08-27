package dk.gatchaz.server.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 회원 정보 (내 정보 조회 공용).
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "회원 정보")
public class MemberDetailResponse {

    @Schema(description = "회원 ID", example = "1")
    private Long memberId;

    @Schema(description = "닉네임", example = "상민")
    private String nickname;

    @Schema(description = "나이", example = "27")
    private Integer age;

    @Schema(description = "프로필 이미지 URL. 설정하지 않았으면 null", example = "https://cdn.travelgatcha.app/profile/1.png")
    private String profileImageUrl;

    @Schema(description = "생성 일시", example = "2026-08-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시. 수정 이력이 없으면 null", example = "2026-08-01T12:00:00")
    private LocalDateTime updatedAt;
}
