package dk.gatchaz.server.notification.controller;

import dk.gatchaz.server.common.dto.ResponseDto;
import dk.gatchaz.server.notification.dto.DiaryReminderResponse;
import dk.gatchaz.server.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 알림 관련 정기 배치를 수동으로 즉시 실행하는 운영용 API.
 * 앱 화면에서 호출하는 API 가 아니므로 알림 도메인 API 와 분리하고, 배치 실행 API 끼리 같은 태그로 묶는다.
 */
@Tag(name = "Batch", description = "배치 수동 실행 API (운영용)")
@RestController
@RequestMapping("/api/v1/batch")
@RequiredArgsConstructor
public class NotificationBatchController {

    private final NotificationService notificationService;

    /**
     * 일기 작성 독려 알림 발송 (배치 수동 실행)
     */
    @Operation(
            summary = "일기 작성 독려 알림 발송",
            description = """
                    여행 기간 중이면서 오늘 일기를 아직 쓰지 않은 참여자에게 일기 작성을 권유하는 알림을 보낸다.
                    **매일 21시(Asia/Seoul)에 자동 실행되는 배치를 수동으로 즉시 실행하는 API 다.**
                    운영용이며 앱 화면에서는 호출하지 않는다.

                    ### 발송 대상 (아래를 모두 만족하는 회원)
                    1. 취소·완료되지 않은 여행에 참여(JOINED) 중
                    2. 오늘이 그 여행의 기간(`start_date` ~ `end_date`) 안
                    3. 오늘 작성한 일기가 없음
                    4. 오늘 이 알림을 아직 받지 않음

                    ### 참고
                    - 조건 4 덕분에 **같은 날 여러 번 호출해도 중복 발송되지 않는다.** 두 번째 호출부터는 0 이 반환된다.
                    - 일기는 회원 기준 하루 1개만 쓸 수 있으므로, 한 회원이 같은 기간에 여러 여행에 참여 중이어도
                      **여행 1개에 대해서만** 알림이 간다.
                    - 알림을 누르면 `targetType = TRIP`, `targetId = 여행 ID` 로 일기 작성 화면을 연다.
                    - FCM 설정이 없으면 인앱 알림함에만 쌓이고 푸시는 발송되지 않는다. (발송 로그에 `SKIPPED` 기록)
                    """)
    @ApiResponse(
            responseCode = "200",
            description = "발송 결과",
            content = @Content(
                    mediaType = "*/*",
                    // 예시를 지정하면 springdoc 이 자동 추론 스키마를 대체하므로 data 부분의 스키마를 명시한다.
                    schema = @Schema(implementation = DiaryReminderResponse.class),
                    examples = {
                            @ExampleObject(name = "발송됨", value = """
                                    {
                                      "success": true,
                                      "data": { "notifiedMemberCount": 3 },
                                      "error": null
                                    }"""),
                            @ExampleObject(name = "대상 없음 (또는 오늘 이미 발송)", value = """
                                    {
                                      "success": true,
                                      "data": { "notifiedMemberCount": 0 },
                                      "error": null
                                    }""")
                    }))
    @PostMapping("/diary-reminders")
    public ResponseDto<DiaryReminderResponse> sendDiaryReminders() {
        return ResponseDto.ok(new DiaryReminderResponse(notificationService.notifyDiaryWriteReminder()));
    }
}
