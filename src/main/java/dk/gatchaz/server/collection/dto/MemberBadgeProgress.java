package dk.gatchaz.server.collection.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 회원의 배지 진행 상태 내부 조회 결과 (없으면 아직 한 번도 진행되지 않은 배지).
 * API 응답용 DTO 가 아니라 서비스 내부에서만 쓰인다.
 */
@Getter
@Setter
@NoArgsConstructor
public class MemberBadgeProgress {

    private Integer currentCount;
    private String achievedYn;
    private LocalDateTime achievedAt;
}
