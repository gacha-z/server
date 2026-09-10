package dk.gatchaz.server.interfaces.controller;

import dk.gatchaz.server.common.dto.ResponseDto;
import dk.gatchaz.server.interfaces.dto.TripRegionImageResponse;
import dk.gatchaz.server.interfaces.service.TripRegionImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 정기 배치를 수동으로 즉시 실행하는 운영용 API.
 * 앱 화면에서 호출하는 API 가 아니므로 도메인 API 와 분리해 둔다.
 */
@Tag(name = "Batch", description = "배치 수동 실행 API (운영용)")
@RestController
@RequestMapping("/api/v1/batch")
@RequiredArgsConstructor
public class BatchController {

    private final TripRegionImageService tripRegionImageService;

    /**
     * 지역 대표 이미지 적재 (배치 수동 실행)
     */
    @Operation(
            summary = "지역 대표 이미지 적재",
            description = """
                    trip_region 의 모든 사용 중(use_yn = 'Y') 지역에 대해 한국관광공사 TourAPI 에서
                    대표 이미지를 조회해 `image_url` 에 저장한다.
                    **매월 1일 04시(Asia/Seoul)에 자동 실행되는 배치를 수동으로 즉시 실행하는 API 다.**
                    운영·데이터 적재용이며 앱 화면에서는 호출하지 않는다.

                    ### 동작 방식
                    1. `trip_region_code`(법정동 코드 10자리)를 시도 코드(앞 2자리) + 시군구 코드(3~5번째 자리)로 잘라
                       TourAPI `areaBasedList2` 를 지역별로 1회씩 호출한다. (예: `5111000000` → 시도 `51`, 시군구 `110`)
                    2. 관광지(contentTypeId = 12)만, 대표 이미지가 있는 항목을 수정일 최신순으로 조회한다.
                    3. 조회된 순서대로 대표 이미지가 있는 **첫 번째 건**의 이미지를 사용하고 나머지는 무시한다.
                       (첫 건의 이미지가 비어 있으면 다음 건을 확인한다)
                    4. 이미 `image_url` 이 채워진 지역도 **새로 조회한 값으로 덮어쓴다.**

                    ### 응답
                    - `totalCount`: 적재 대상 지역 수
                    - `successCount` / `failCount`: 성공·실패 지역 수
                    - `failures`: 실패한 지역의 ID, 지역명, 실패 사유 목록 (모두 성공하면 빈 배열)

                    ### 참고
                    - 지역 하나가 실패해도 나머지 지역은 계속 처리되므로, 부분 실패 시에도 응답은 200 이다.
                      실제 결과는 `failCount` 와 `failures` 로 확인한다.
                    - 지역 수만큼 외부 API 를 순차 호출하므로 지역이 많으면 응답까지 시간이 걸릴 수 있다.
                    """)
    @ApiResponse(
            responseCode = "200",
            description = "적재 결과 요약",
            content = @Content(
                    mediaType = "*/*",
                    // 예시를 지정하면 springdoc 이 자동 추론 스키마를 대체하므로 data 부분의 스키마를 명시한다.
                    schema = @Schema(implementation = TripRegionImageResponse.class),
                    examples = {
                            @ExampleObject(name = "전부 성공", value = """
                                    {
                                      "success": true,
                                      "data": { "totalCount": 230, "successCount": 230, "failCount": 0, "failures": [] },
                                      "error": null
                                    }"""),
                            @ExampleObject(name = "일부 실패", value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "totalCount": 230,
                                        "successCount": 229,
                                        "failCount": 1,
                                        "failures": [
                                          {
                                            "tripRegionId": 9,
                                            "tripRegionName": "강원특별자치도 횡성군",
                                            "reason": "대표 이미지가 있는 관광지를 찾지 못했습니다."
                                          }
                                        ]
                                      },
                                      "error": null
                                    }""")
                    }))
    @PostMapping("/trip-region-images")
    public ResponseDto<TripRegionImageResponse> updateTripRegionImages() {
        return ResponseDto.ok(tripRegionImageService.updateRegionImages());
    }
}
