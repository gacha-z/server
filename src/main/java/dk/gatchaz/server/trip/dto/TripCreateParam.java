package dk.gatchaz.server.trip.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * trip 생성(INSERT) 용 파라미터. 요청 DTO 와 서비스 계층의 보정값(ownerMemberId, status)을 분리한다.
 */
@Getter
@Setter
@Builder
public class TripCreateParam {

    /** 생성된 여행 ID (INSERT 후 채워짐) */
    private Long tripId;

    private Long ownerMemberId;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer memberLimit;
    private Integer missionMin;
    private Integer missionMax;
    private LocalDateTime missionStartAt;
    private String status;
}
