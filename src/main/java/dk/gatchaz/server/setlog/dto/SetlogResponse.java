package dk.gatchaz.server.setlog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "셋로그 목록 항목")
public class SetlogResponse {

    @Schema(description = "셋로그 ID", example = "1")
    private Long setlogId;

    @Schema(description = "여행 ID", example = "1")
    private Long tripId;

    @Schema(description = "진행 미션(trip_mission) ID", example = "1")
    private Long tripMissionId;

    @Schema(description = "촬영한 회원 ID", example = "1")
    private Long memberId;

    @Schema(description = "촬영한 회원 닉네임")
    private String memberNickname;

    @Schema(description = "S3 에 저장된 영상 URL")
    private String fileUrl;

    @Schema(description = "렌더링 위치(슬롯 번호)", example = "1")
    private int slotNo;

    @Schema(description = "업로드 일시")
    private LocalDateTime createdAt;
}
