package dk.gatchaz.server.trip.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

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

    /** 여행 시작일 (참여자의 다른 여행과 기간 겹침 검증용) */
    private LocalDate startDate;

    /** 여행 종료일 (참여자의 다른 여행과 기간 겹침 검증용) */
    private LocalDate endDate;
}
