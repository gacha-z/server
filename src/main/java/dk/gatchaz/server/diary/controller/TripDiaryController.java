package dk.gatchaz.server.diary.controller;

import dk.gatchaz.server.common.annotation.UserId;
import dk.gatchaz.server.common.dto.ResponseDto;
import dk.gatchaz.server.diary.dto.DiaryDetailResponse;
import dk.gatchaz.server.diary.dto.DiarySummaryResponse;
import dk.gatchaz.server.diary.service.DiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 같은 여행(trip) 참여자(본인 포함)의 일기를 조회하는 API. 요청자가 그 여행에 참여 중이 아니면 조회할 수 없다.
 * (visibility 는 아직 반영하지 않음 - 추후 확장 여지로 남겨둠)
 */
@Tag(name = "Diary", description = "일기 API")
@RestController
@RequestMapping("/api/v1/trips/{tripId}/diaries")
@RequiredArgsConstructor
public class TripDiaryController {

    private final DiaryService diaryService;

    /**
     * 여행 팀원 일기 목록 조회 (날짜별)
     */
    @Operation(summary = "여행 팀원 일기 목록 조회 (날짜별)",
            description = "같은 여행(tripId) 참여자 전체(본인 포함)가 특정 날짜(diaryDate)에 쓴 일기 요약 목록을 반환한다. "
                    + "예: 1일차 일기를 보려면 그 날의 diaryDate 를, 2일차면 다음 날의 diaryDate 를 넘긴다. "
                    + "본문/공개범위/AI 생성여부/상태는 포함하지 않으며(목록에서는 누가 썼는지만 확인), 필요하면 "
                    + "GET /trips/{tripId}/diaries/{diaryId} 로 단건 상세를 조회한다. "
                    + "요청자가 그 여행에 참여 중이 아니면 404(NOT_FOUND_TRIP_MEMBER)를 반환한다.")
    @GetMapping
    public ResponseDto<List<DiarySummaryResponse>> getTripDiariesByDate(
            @UserId final Long userId,
            @Parameter(description = "여행 ID", example = "1") @PathVariable final Long tripId,
            @Parameter(description = "조회할 일기 날짜 (yyyy-MM-dd)", example = "2026-08-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate diaryDate) {
        return ResponseDto.ok(diaryService.getTripDiariesByDate(tripId, diaryDate, userId));
    }

    /**
     * 여행 팀원 일기 상세 조회
     */
    @Operation(summary = "여행 팀원 일기 상세 조회",
            description = "같은 여행(tripId) 참여자(본인 포함) 누구의 것이든 일기 1건을 조회한다. "
                    + "요청자가 그 여행에 참여 중이 아니면 404(NOT_FOUND_TRIP_MEMBER), "
                    + "일기가 없거나 그 여행 소속이 아니면 404(NOT_FOUND_DIARY)를 반환한다.")
    @GetMapping("/{diaryId}")
    public ResponseDto<DiaryDetailResponse> getTripDiary(
            @UserId final Long userId,
            @Parameter(description = "여행 ID", example = "1") @PathVariable final Long tripId,
            @Parameter(description = "조회할 일기 ID", example = "1") @PathVariable final Long diaryId) {
        return ResponseDto.ok(diaryService.getTripDiary(tripId, diaryId, userId));
    }
}
