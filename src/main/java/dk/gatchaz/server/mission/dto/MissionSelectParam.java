package dk.gatchaz.server.mission.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * trip_mission 생성(INSERT) 용 파라미터.
 */
@Getter
@Setter
@Builder
public class MissionSelectParam {

    /** 생성된 trip_mission ID (INSERT 후 채워짐) */
    private Long tripMissionId;

    private Long tripId;
    private Long missionId;
    private int dayNo;
    private int assignedOrder;
    private String status;
}
