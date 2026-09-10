package dk.gatchaz.server.collection.controller;

import dk.gatchaz.server.collection.dto.BadgeResponse;
import dk.gatchaz.server.collection.dto.CollectionItemResponse;
import dk.gatchaz.server.collection.service.CollectionService;
import dk.gatchaz.server.common.annotation.UserId;
import dk.gatchaz.server.common.dto.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Collection", description = "도감(아이템/배지) API")
@RestController
@RequestMapping("/api/v1/collection")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    /**
     * 아이템 도감 조회 (Read → param)
     */
    @Operation(
            summary = "아이템 도감 목록 조회",
            description = "전체 지역 아이템 목록을 회원 기준 보유 여부(acquiredYn)와 함께 반환한다.")
    @GetMapping("/items")
    public ResponseDto<List<CollectionItemResponse>> getItems(@UserId final Long memberId) {
        return ResponseDto.ok(collectionService.getItems(memberId));
    }

    /**
     * 배지함 조회 (Read → param)
     */
    @Operation(
            summary = "배지 목록 조회",
            description = "전체 배지 목록을 회원 기준 진행 횟수(currentCount)/달성 여부(achievedYn)와 함께 반환한다.")
    @GetMapping("/badges")
    public ResponseDto<List<BadgeResponse>> getBadges(@UserId final Long memberId) {
        return ResponseDto.ok(collectionService.getBadges(memberId));
    }
}
