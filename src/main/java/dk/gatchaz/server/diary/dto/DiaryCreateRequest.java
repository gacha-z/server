package dk.gatchaz.server.diary.dto;

import dk.gatchaz.server.type.EDiaryVisibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "일기 저장 요청. content 를 그대로 저장한다. AI 생성 초안을 저장할 땐 isAiGenerated=true 로 보낸다.")
public class DiaryCreateRequest {

    /** 연결할 여행 ID. (필수, diary.trip_id 는 NOT NULL) */
    @Schema(description = "연결할 여행 ID (필수)", example = "1")
    @NotNull
    private Long tripId;

    /** 작성자 회원 ID. (필수, diary.member_id 는 NOT NULL) 로그인 연동 전까지 요청으로 받는다. */
    @Schema(description = "작성자 회원 ID (필수). 로그인 연동 전까지 요청으로 받는다.", example = "1")
    @NotNull
    private Long memberId;

    /** 저장할 일기 본문. 직접 작성한 내용 또는 /generate 로 받은 AI 초안. */
    @Schema(description = "저장할 일기 본문. 직접 작성 내용 또는 /generate 로 받은 AI 초안.", example = "안목해변에서 커피 마시고 노을 봄. 친구랑 오랜만에 수다 떨어서 좋았음")
    private String content;

    /** 일기 날짜 (필수). 회원 기준 하루 1개만 작성 가능하다. */
    @Schema(description = "일기 날짜 (필수). 회원 기준 하루 1개만 작성 가능(중복 시 409).", example = "2026-08-01")
    @NotNull
    private LocalDate diaryDate;

    /** 공개 범위(선택). 미지정 시 TEAM 으로 저장된다. */
    @Schema(description = "공개 범위(선택, 미지정 시 TEAM). PRIVATE: 본인만 / TEAM: 여행 참여자 / PUBLIC: 전체", example = "TEAM")
    private EDiaryVisibility visibility;

    /** AI 생성 여부(선택, 기본 false). /generate 로 만든 AI 초안을 저장할 때 true 로 보내면 is_ai_generated='Y' 로 기록된다. */
    @Schema(description = "AI 생성 여부(선택, 기본 false). AI 초안 저장 시 true → is_ai_generated='Y'.", example = "false")
    private Boolean isAiGenerated;

    /** [AI 저장 시] AI 생성에 사용한 원본 입력(/generate 에 보낸 content). isAiGenerated=true 면 diary_ai_generation.source_content 로 기록된다. */
    @Schema(description = "[AI 저장 시 권장] AI 생성에 사용한 원본 입력. isAiGenerated=true 일 때 생성 이력(diary_ai_generation)의 source_content 로 기록된다.", example = "안목해변 커피 노을 친구랑 수다")
    private String sourceContent;
}
