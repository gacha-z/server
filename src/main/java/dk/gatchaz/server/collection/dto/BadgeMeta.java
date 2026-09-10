package dk.gatchaz.server.collection.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 배지 지급 판단용 내부 조회 결과 (badge_code 기준 badge_id / target_count).
 * API 응답용 DTO 가 아니라 서비스 내부에서만 쓰인다.
 */
@Getter
@Setter
@NoArgsConstructor
public class BadgeMeta {

    private Long badgeId;
    private Integer targetCount;
}
