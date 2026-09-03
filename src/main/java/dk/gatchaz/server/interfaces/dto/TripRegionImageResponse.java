package dk.gatchaz.server.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 지역 대표 이미지 적재 결과 요약.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "지역 대표 이미지 적재 결과")
public class TripRegionImageResponse {

    @Schema(description = "적재 대상 지역 수 (use_yn = 'Y')", example = "230")
    private int totalCount;

    @Schema(description = "image_url 갱신에 성공한 지역 수", example = "229")
    private int successCount;

    @Schema(description = "실패한 지역 수 (failures 배열의 크기와 항상 같다)", example = "1")
    private int failCount;

    @Schema(description = "실패한 지역 상세 목록. 모두 성공하면 빈 배열이다.")
    private List<TripRegionImageFailure> failures;
}
