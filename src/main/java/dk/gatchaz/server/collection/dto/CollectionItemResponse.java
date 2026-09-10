package dk.gatchaz.server.collection.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 도감 아이템 1개 (전체 목록 + 회원 기준 보유 여부).
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "도감 아이템")
public class CollectionItemResponse {

    @Schema(description = "아이템 ID", example = "1")
    private Long collectionItemId;

    @Schema(description = "소속 지역 그룹 코드", example = "SEOUL")
    private String regionGroupCode;

    @Schema(description = "소속 지역 그룹명", example = "서울")
    private String regionGroupName;

    @Schema(description = "아이템 이름", example = "서울의 기념품")
    private String itemName;

    @Schema(description = "아이템 유형", example = "REGION_SOUVENIR")
    private String itemType;

    @Schema(description = "아이템 이미지 URL")
    private String imageUrl;

    @Schema(description = "아이템 설명")
    private String description;

    @Schema(description = "보유 여부 (Y/N)", example = "N")
    private String acquiredYn;

    @Schema(description = "획득 일시. 미보유면 null")
    private LocalDateTime acquiredAt;
}
