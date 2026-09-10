package dk.gatchaz.server.mission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 미션 완료 요청. 완료 버튼을 누른 시점의 위치로 셋로그 완료 여부와 함께 위치 인증을 진행한다.
 */
@Getter
@Setter
@NoArgsConstructor
public class MissionCompleteRequest {

    @Schema(description = "완료 시점 위도", example = "33.4996213")
    @NotNull
    private BigDecimal latitude;

    @Schema(description = "완료 시점 경도", example = "126.5311884")
    @NotNull
    private BigDecimal longitude;
}
