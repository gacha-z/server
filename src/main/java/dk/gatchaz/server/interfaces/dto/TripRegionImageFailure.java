package dk.gatchaz.server.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 지역 대표 이미지 적재에 실패한 지역의 상세 정보.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "지역 대표 이미지 적재 실패 상세")
public class TripRegionImageFailure {

    @Schema(description = "여행 지역 ID", example = "9")
    private Long tripRegionId;

    @Schema(description = "여행 지역명", example = "강원특별자치도 횡성군")
    private String tripRegionName;

    @Schema(description = "실패 사유", example = "대표 이미지가 있는 관광지를 찾지 못했습니다.")
    private String reason;
}
