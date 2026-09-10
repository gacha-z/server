package dk.gatchaz.server.diary.controller;

import dk.gatchaz.server.diary.dto.DiaryCreateRequest;
import dk.gatchaz.server.diary.dto.DiaryDetailResponse;
import dk.gatchaz.server.diary.dto.DiaryGenerateRequest;
import dk.gatchaz.server.diary.dto.DiaryGenerateResponse;
import dk.gatchaz.server.diary.dto.DiaryListResponse;
import dk.gatchaz.server.diary.dto.DiarySearchRequest;
import dk.gatchaz.server.diary.dto.DiaryUpdateRequest;
import dk.gatchaz.server.diary.service.DiaryService;
import dk.gatchaz.server.common.dto.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "Diary", description = "일기 API")
@RestController
@RequestMapping("/api/v1/diaries")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    /**
     * 일기 저장
     */
    @Operation(summary = "일기 저장", description = "요청의 content 를 그대로 저장한다(생성 로직 없음). 직접 작성한 내용 또는 /generate 로 받은 AI 초안을 저장한다. AI 초안 저장 시 isAiGenerated=true 로 보내면 is_ai_generated='Y' 로 기록된다(미지정 시 'N'). tripId·memberId 필수, visibility 미지정 시 TEAM. 저장된 일기 전체를 반환한다.")
    @PostMapping
    public ResponseDto<DiaryDetailResponse> createDiary(@Valid @RequestBody final DiaryCreateRequest request) {
        return ResponseDto.created(diaryService.createDiary(request));
    }

    /**
     * AI 일기 초안 생성 (저장하지 않음)
     */
    @Operation(summary = "AI 일기 초안 생성(미리보기)", description = "content(사용자 입력)로 AI가 본문 초안을 생성해 반환한다(저장하지 않음). 사용자가 검토·수정 후 완료 시, POST /diaries 에 content=초안, isAiGenerated=true 로 호출해 저장한다. 무료 티어(Groq) 사용. 키 미설정/한도초과/오류 시 503(AI_GENERATION_FAILED).")
    @PostMapping("/generate")
    public ResponseDto<DiaryGenerateResponse> generateDiary(@Valid @RequestBody final DiaryGenerateRequest request) {
        return ResponseDto.ok(diaryService.generateDiary(request));
    }

    /**
     * 일기 단건 조회
     */
    @Operation(summary = "일기 상세 조회", description = "일기 ID로 단건을 조회한다. 삭제된 일기는 조회되지 않으며, 없으면 404 를 반환한다.")
    @GetMapping("/{diaryId}")
    public ResponseDto<DiaryDetailResponse> getDiary(
            @Parameter(description = "조회할 일기 ID", example = "1") @PathVariable final Long diaryId) {
        return ResponseDto.ok(diaryService.getDiary(diaryId));
    }

    /**
     * 일기 목록 조회 (커서 기반 무한 스크롤)
     */
    @Operation(
            summary = "일기 목록 조회",
            description = """
                    회원/여행 조건으로 일기를 최신순, 커서 기반 무한 스크롤로 조회한다. 삭제된 일기는 제외된다.

                    ### 필터 (모두 선택값, 없으면 조건 무시)
                    - **memberId**: 조회 기준 회원 ID (없으면 전체 대상)
                    - **tripId**: 연결된 여행 ID 정확 일치

                    ### 무한 스크롤 (커서 방식)
                    1. 첫 조회는 `cursor` 없이 호출한다.
                    2. 응답의 `nextCursor` 를 그대로 다음 요청의 `cursor` 로 넘긴다.
                    3. `hasNext = false` (이때 `nextCursor = null`) 이면 마지막 페이지이므로 추가 호출을 멈춘다.
                    """)
    @GetMapping
    public ResponseDto<DiaryListResponse> getDiaries(
            @Parameter(description = "조회 기준 회원 ID(선택). 없으면 전체 대상.", example = "1")
            @RequestParam(required = false) final Long memberId,
            @Parameter(description = "연결된 여행 ID(선택, 정확히 일치)", example = "1")
            @RequestParam(required = false) final Long tripId,
            @Parameter(description = "일기 날짜(선택, yyyy-MM-dd). 특정 날짜의 일기 조회.", example = "2026-08-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate diaryDate,
            @Parameter(description = "무한 스크롤 커서. 이전 응답의 nextCursor 값을 넣는다. 첫 조회 시 비운다.", example = "31")
            @RequestParam(required = false) final Long cursor,
            @Parameter(description = "한 번에 조회할 개수 (기본 10, 1~50 범위를 벗어나면 자동 보정)", example = "10")
            @RequestParam(required = false, defaultValue = "10") final int size) {
        final DiarySearchRequest request = new DiarySearchRequest(memberId, tripId, diaryDate, cursor, size);
        return ResponseDto.ok(diaryService.getDiaries(request));
    }

    /**
     * 일기 수정 (전달한 값으로 본문/공개범위 교체)
     */
    @Operation(summary = "일기 수정", description = "일기 ID의 본문/공개범위를 전달한 값으로 교체하고, 수정된 일기를 반환한다. 삭제된 일기는 수정할 수 없어 404 를 반환한다.")
    @PatchMapping("/{diaryId}")
    public ResponseDto<DiaryDetailResponse> updateDiary(
            @Parameter(description = "수정할 일기 ID", example = "1") @PathVariable final Long diaryId,
            @Valid @RequestBody final DiaryUpdateRequest request) {
        return ResponseDto.ok(diaryService.updateDiary(diaryId, request));
    }

    /**
     * 일기 삭제 (소프트 삭제)
     */
    @Operation(summary = "일기 삭제", description = "일기 ID로 일기를 소프트 삭제한다(status='DELETED'). 이미 삭제되었거나 없으면 404 를 반환한다.")
    @DeleteMapping("/{diaryId}")
    public ResponseDto<Void> deleteDiary(
            @Parameter(description = "삭제할 일기 ID", example = "1") @PathVariable final Long diaryId) {
        diaryService.deleteDiary(diaryId);
        return ResponseDto.<Void>ok(null);
    }
}
