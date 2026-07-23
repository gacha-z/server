package dk.gatchaz.server.trip.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 초대 코드로 여행을 조회할 때 참여 검증에 필요한 최소 정보.
 */
@Getter
@Setter
public class TripJoinInfo {

    private Long tripId;

    /** 여행 상태 (ETripStatus) */
    private String status;

    /** 여행 정원 */
    private Integer memberLimit;
}
