package dk.gatchaz.server.diary.dto;

import dk.gatchaz.server.type.EDiaryVisibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "일기 생성 요청")
public class DiaryCreateRequest {

    /** 연결할 여행 ID. (필수, diary.trip_id 는 NOT NULL) */
    @Schema(description = "연결할 여행 ID (필수)", example = "42")
    @NotNull
    private Long tripId;

    /** 작성자 회원 ID. (필수, diary.member_id 는 NOT NULL) 로그인 연동 전까지 요청으로 받는다. */
    @Schema(description = "작성자 회원 ID (필수). 로그인 연동 전까지 요청으로 받는다.", example = "1")
    @NotNull
    private Long memberId;

    /** 일기 본문. 추후 생성형 AI 가 채울 수 있도록 선택값으로 둔다. */
    @Schema(description = "일기 본문. 미입력 가능(추후 생성형 AI 로 채우는 확장 대비).", example = "오늘은 강릉 안목해변에서 커피를 마셨다.")
    private String content;

    /** 공개 범위(선택). 미지정 시 TEAM 으로 저장된다. */
    @Schema(description = "공개 범위(선택, 미지정 시 TEAM). PRIVATE: 본인만 / TEAM: 여행 참여자 / PUBLIC: 전체", example = "TEAM")
    private EDiaryVisibility visibility;
}
