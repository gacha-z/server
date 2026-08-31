package dk.gatchaz.server.setlog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "셋로그 업로드 응답")
public class SetlogUploadResponse {

    @Schema(description = "생성된 셋로그 ID", example = "1")
    private Long setlogId;

    @Schema(description = "진행 미션(trip_mission) ID", example = "1")
    private Long tripMissionId;

    @Schema(description = "촬영한 회원 ID", example = "1")
    private Long memberId;

    @Schema(description = "S3 에 저장된 영상 URL")
    private String fileUrl;

    @Schema(description = "서버가 자동 배정한 렌더링 위치(슬롯 번호)", example = "1")
    private int slotNo;

    @Schema(description = "업로드 일시")
    private LocalDateTime createdAt;
}
