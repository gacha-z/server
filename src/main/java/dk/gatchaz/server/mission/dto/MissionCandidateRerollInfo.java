package dk.gatchaz.server.mission.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 리롤(reroll) 처리에 필요한 후보 정보.
 */
@Getter
@Setter
@NoArgsConstructor
public class MissionCandidateRerollInfo {
    private Long missionCandidateId;
    private Long missionId;
    private Long pickerMemberId;
    private int dayNo;
    private int assignedOrder;
    private Integer rerollCount;
}
