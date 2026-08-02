package dk.gatchaz.server.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 일기 목록 조회(검색) 요청. 필터는 선택값이며, 값이 없으면 해당 조건은 무시된다.
 * 커서 기반 무한 스크롤로 조회한다. (cursor = 이전 페이지 마지막 일기 ID, 첫 조회 시 비움)
 */
@Getter
@AllArgsConstructor
public class DiarySearchRequest {

    /** 조회 기준 회원 ID (선택) */
    private Long memberId;

    /** 연결된 여행 ID (정확히 일치, 선택) */
    private Long tripId;

    /** 일기 날짜 (정확히 일치, 선택). 특정 날짜의 일기 조회용. */
    private LocalDate diaryDate;

    /** 무한 스크롤 커서 (이전 페이지 마지막 일기 ID). 첫 조회 시 비운다. */
    private Long cursor;

    /** 한 번에 조회할 개수 (1~50, 범위를 벗어나면 서비스에서 보정) */
    private int size;
}
