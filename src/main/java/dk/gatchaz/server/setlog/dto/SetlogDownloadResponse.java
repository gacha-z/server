package dk.gatchaz.server.setlog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "셋로그 다운로드 응답")
public class SetlogDownloadResponse {

    @Schema(description = "다운로드할 영상 URL")
    private String fileUrl;
}
