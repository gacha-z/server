package dk.gatchaz.server.mission.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 미션 후보 생성/조회에 필요한 여행의 미션 관련 설정 정보.
 */
@Getter
@Setter
@NoArgsConstructor
public class TripMissionSettingInfo {

    private Long tripId;

    /** 선택된 여행 지역 ID. 지역 미선택 상태면 null */
    private Long tripRegionId;

    /** 하루 최소 미션 수 */
    private Integer missionMin;

    /** 하루 최대 미션 수 */
    private Integer missionMax;

    /** 첫 미션 받을 일시 */
    private LocalDateTime missionStartAt;

    /** 여행 종료일 (마지막 날짜까지가 미션 진행 기간 - 여행 완료 판정에 사용) */
    private LocalDateTime endDate;
}
