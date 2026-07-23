package dk.gatchaz.server.trip.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 여행 목록 조회 응답. 커서 기반 무한 스크롤용 정보를 함께 반환한다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "여행 목록 조회 응답 (커서 기반 무한 스크롤)")
public class TripListResponse {

    @Schema(description = "조회된 여행 목록 (최신순)")
    private List<TripSummaryResponse> trips;

    @Schema(description = "다음 조회에 사용할 커서. 다음 요청의 cursor 파라미터에 그대로 넣는다. 다음 페이지가 없으면 null", example = "31")
    private Long nextCursor;

    @Schema(description = "다음 페이지 존재 여부. false 이면 마지막 페이지이므로 추가 호출을 멈춘다.", example = "true")
    private boolean hasNext;
}
