package dk.gatchaz.server.mission.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * mission 테이블 기본 정보.
 */
@Getter
@Setter
@NoArgsConstructor
public class MissionInfo {
    private Long missionId;
    private String missionType;
    private String title;
    private String description;
    private Integer difficulty;
}
