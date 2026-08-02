package dk.gatchaz.server.diary.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 일기 목록 조회(SELECT) 용 파라미터. size 에는 다음 페이지 존재 여부(hasNext) 판별을 위해 요청 개수 + 1 이 담긴다.
 */
@Getter
@Setter
@Builder
public class DiarySearchParam {

    /** 조회 기준 회원 ID (선택) */
    private Long memberId;

    /** 연결된 여행 ID (선택) */
    private Long tripId;

    /** 무한 스크롤 커서 (이전 페이지 마지막 일기 ID) */
    private Long cursor;

    /** 조회 개수 (요청 size + 1) */
    private int size;
}
