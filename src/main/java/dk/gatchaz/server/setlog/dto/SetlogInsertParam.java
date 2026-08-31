package dk.gatchaz.server.setlog.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * setlog INSERT 용 파라미터.
 */
@Getter
@Setter
@Builder
public class SetlogInsertParam {

    /** 생성된 setlog ID (INSERT 후 채워짐) */
    private Long setlogId;

    private Long tripId;
    private Long tripMissionId;
    private Long memberId;
    private String fileUrl;
    private int slotNo;
    private String status;
}
