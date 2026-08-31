package dk.gatchaz.server.mission.dto;

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
@Schema(description = "미션 선택 응답")
public class MissionSelectResponse {

    @Schema(description = "생성된 진행 미션(trip_mission) ID", example = "1")
    private Long tripMissionId;

    @Schema(description = "미션 ID", example = "10")
    private Long missionId;

    @Schema(description = "미션 유형", example = "PHOTO")
    private String missionType;

    @Schema(description = "미션 제목", example = "노을 지는 바다 사진 찍기")
    private String title;

    @Schema(description = "미션 설명")
    private String description;

    @Schema(description = "미션 난이도", example = "2")
    private Integer difficulty;

    @Schema(description = "미션 시작 일시")
    private LocalDateTime startedAt;
}
