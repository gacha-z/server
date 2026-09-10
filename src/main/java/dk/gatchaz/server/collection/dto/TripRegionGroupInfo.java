package dk.gatchaz.server.collection.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 여행이 선택한 지역이 속한 지역 그룹 정보 (여행 완료 시 지역별 배지/아이템 지급 판단용).
 * API 응답용 DTO 가 아니라 서비스 내부에서만 쓰인다.
 */
@Getter
@Setter
@NoArgsConstructor
public class TripRegionGroupInfo {

    private Long regionGroupId;
    private String regionGroupCode;
}
