package dk.gatchaz.server.collection.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 특정 여행에서 회원별로 작성한 일기 개수. 여행 취소 시 일기 관련 배지 진행도를 되돌릴 때 쓰인다.
 * API 응답용 DTO 가 아니라 서비스 내부에서만 쓰인다.
 */
@Getter
@Setter
@NoArgsConstructor
public class MemberDiaryCount {

    private Long memberId;
    private Integer diaryCount;
}
