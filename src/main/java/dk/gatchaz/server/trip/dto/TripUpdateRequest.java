package dk.gatchaz.server.trip.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 여행 정보 부분 수정 요청. 포함된(null 이 아닌) 필드만 수정하고 나머지는 기존 값을 유지한다.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "여행 정보 수정 요청. 수정할 필드만 보내면 되고, 보내지 않은 필드는 기존 값이 유지된다.")
public class TripUpdateRequest {

    @Schema(description = "여행 제목. 보내는 경우 공백일 수 없다.", example = "제주 여름 여행")
    private String title;

    @Schema(description = "여행 시작일. 변경 시 첫 미션 일시의 날짜도 함께 이동한다.", example = "2026-07-10")
    private LocalDate startDate;

    @Schema(description = "여행 종료일", example = "2026-07-12")
    private LocalDate endDate;

    @Schema(description = "여행 정원. 현재 참여 인원보다 작게 줄일 수 없다.", example = "6")
    @Min(1)
    private Integer memberLimit;

    @Schema(description = "하루 최소 미션 수", example = "1")
    @Min(1)
    private Integer missionMin;

    @Schema(description = "하루 최대 미션 수", example = "3")
    @Min(1)
    private Integer missionMax;

    @Schema(description = "첫 미션 받을 시각 (시·분만, 예: \"10:00\"). 최종 여행 시작일과 합쳐 첫 미션 일시로 저장된다.", example = "10:00", type = "string")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime missionStartTime;
}
