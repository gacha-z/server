package dk.gatchaz.server.mission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 미션 후보 1개.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MissionCandidateResponse {

    @Schema(description = "미션 후보 ID", example = "1")
    private Long missionCandidateId;

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

    @Schema(description = "선택 여부 (Y/N)", example = "N")
    private String selectedYn;

    @Schema(description = "리롤되어 교체된(비활성) 후보인지 여부 (Y/N). Y 면 이미 리롤되어 더 이상 유효하지 않다.", example = "N")
    private String rerolledYn;
}
