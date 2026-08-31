package dk.gatchaz.server.mission.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 리롤로 새로 생기는 mission_candidate 행 INSERT 용 파라미터.
 */
@Getter
@Setter
@Builder
public class MissionRerollInsertParam {

    /** 생성된 mission_candidate ID (INSERT 후 채워짐) */
    private Long missionCandidateId;

    private Long tripId;
    private int dayNo;
    private int assignedOrder;
    private Long missionId;

    /** 새로 생긴 후보의 남은 리롤 횟수 (항상 0 - 리롤로 생긴 후보는 다시 리롤할 수 없다) */
    private int rerollCount;
}
