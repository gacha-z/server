package dk.gatchaz.server.trip.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
public class TripCreateRequest {

    /** 여행 제목 */
    @NotBlank
    private String title;

    /** 여행 시작일 */
    @NotNull
    private LocalDate startDate;

    /** 여행 종료일 */
    @NotNull
    private LocalDate endDate;

    /** 여행 인원 */
    @NotNull
    @Min(1)
    private Integer memberLimit;

    /** 하루 최소 미션수 */
    @NotNull
    @Min(1)
    private Integer missionMin;

    /** 하루 최대 미션수 */
    @NotNull
    @Min(1)
    private Integer missionMax;

    /** 첫 미션 받을 시각 (시·분만, 예: "10:00"). 여행 시작일과 합쳐 mission_start_at 으로 저장된다. */
    @NotNull
    @JsonFormat(pattern = "HH:mm")
    private LocalTime missionStartTime;
}
