package dk.gatchaz.server.trip.dto;

import dk.gatchaz.server.type.ETripStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 여행 목록 조회(검색) 요청. 모든 필터는 선택값이며, 값이 없으면 해당 조건은 무시된다.
 * 커서 기반 무한 스크롤로 조회한다. (cursor = 이전 페이지 마지막 여행 ID, 첫 조회 시 비움)
 */
@Getter
@AllArgsConstructor
public class TripSearchRequest {

    /** 여행 이름 (부분 일치 검색) */
    private String title;

    /** 여행 지역 ID (정확히 일치) */
    private Long tripRegionId;

    /** 여행 상태 (CREATED / CANCELLED / COMPLETED) */
    private ETripStatus status;

    /** 검색 기간 시작일. 여행 기간(startDate~endDate)이 [dateFrom, dateTo]와 겹치면 조회된다. */
    private LocalDate dateFrom;

    /** 검색 기간 종료일 */
    private LocalDate dateTo;

    /** 무한 스크롤 커서 (이전 페이지 마지막 여행 ID). 첫 조회 시 비운다. */
    private Long cursor;

    /** 한 번에 조회할 개수 (1~50, 범위를 벗어나면 서비스에서 보정) */
    private int size;

    /** 조회 기준 회원 ID. 로그인 연동 전까지 요청으로 받는 선택값이다. */
    private Long memberId;
}
