package dk.gatchaz.server.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 일기 작성 독려 알림 발송 결과.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "일기 작성 독려 알림 발송 결과")
public class DiaryReminderResponse {

    @Schema(description = "알림을 보낸 회원 수. 대상이 없으면 0", example = "3")
    private int notifiedMemberCount;
}
