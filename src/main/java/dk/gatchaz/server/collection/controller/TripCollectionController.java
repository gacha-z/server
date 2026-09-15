package dk.gatchaz.server.collection.controller;

import dk.gatchaz.server.collection.dto.CollectionItemResponse;
import dk.gatchaz.server.collection.service.CollectionService;
import dk.gatchaz.server.common.annotation.UserId;
import dk.gatchaz.server.common.dto.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 특정 여행에서 획득한 도감 아이템을 조회하는 API. 요청자가 그 여행에 참여 중이 아니면 조회할 수 없다.
 */
@Tag(name = "Collection", description = "도감(아이템/배지) API")
@RestController
@RequestMapping("/api/v1/trips/{tripId}/collection-items")
@RequiredArgsConstructor
public class TripCollectionController {

    private final CollectionService collectionService;

    /**
     * 여행에서 획득한 아이템 조회
     */
    @Operation(
            summary = "여행에서 획득한 아이템 조회",
            description = """
                    이 여행(tripId)에서 요청자가 획득한 도감 아이템만 반환한다. (지역 그룹당 아이템이 1개라
                    보통 0개 또는 1개다) 여행 완료 시 방문 지역 아이템을 이미 보유하고 있었으면 그 여행으로는
                    새로 지급되지 않으므로 빈 배열이 반환될 수 있다.

                    요청자가 그 여행 참여자가 아니면 **404** (NOT_FOUND_TRIP_MEMBER)를 반환한다.
                    """)
    @GetMapping
    public ResponseDto<List<CollectionItemResponse>> getTripCollectionItems(
            @UserId final Long userId,
            @Parameter(description = "여행 ID", example = "1") @PathVariable final Long tripId) {
        return ResponseDto.ok(collectionService.getItemsByTrip(tripId, userId));
    }
}
