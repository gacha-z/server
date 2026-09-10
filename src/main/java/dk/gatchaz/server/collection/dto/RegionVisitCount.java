package dk.gatchaz.server.collection.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 회원이 완료한 여행들을 지역 그룹 기준으로 묶어 몇 번씩 방문했는지 나타낸다.
 * 지역 탐험 배지(서로 다른 지역 방문 수 / 동일 지역 방문 수) 판단에 쓰인다.
 * API 응답용 DTO 가 아니라 서비스 내부에서만 쓰인다.
 */
@Getter
@Setter
@NoArgsConstructor
public class RegionVisitCount {

    private Long regionGroupId;
    private Integer visitCount;
}
