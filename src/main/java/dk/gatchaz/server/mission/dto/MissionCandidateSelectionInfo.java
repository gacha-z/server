package dk.gatchaz.server.mission.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 미션 선택(select) 처리에 필요한 후보 정보. (mission_candidate + mission 조인)
 */
@Getter
@Setter
@NoArgsConstructor
public class MissionCandidateSelectionInfo {
    private Long missionCandidateId;
    private Long missionId;
    private Long pickerMemberId;
    private int dayNo;
    private int assignedOrder;
    private String missionType;
    private String title;
    private String description;
    private Integer difficulty;
}
