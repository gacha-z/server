package dk.gatchaz.server.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "기기 등록/갱신 응답")
public class DeviceRegisterResponse {

    @Schema(description = "등록되거나 갱신된 기기 ID", example = "1")
    private Long deviceId;
}
