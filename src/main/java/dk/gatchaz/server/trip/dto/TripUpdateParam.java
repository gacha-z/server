package dk.gatchaz.server.trip.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * trip 부분 수정(UPDATE) 용 파라미터. null 인 필드는 수정하지 않고 기존 값을 유지한다.
 */
@Getter
@Builder
public class TripUpdateParam {

    private Long tripId;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer memberLimit;
    private Integer missionMin;
    private Integer missionMax;

    /** 첫 미션 일시. 시작일 또는 미션 시작 시각이 바뀐 경우에만 재계산되어 채워진다. */
    private LocalDateTime missionStartAt;
}
