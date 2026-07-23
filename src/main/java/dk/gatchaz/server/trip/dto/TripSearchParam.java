package dk.gatchaz.server.trip.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 여행 목록 조회(SELECT) 용 파라미터. 요청 DTO 와 서비스 계층의 보정값(memberId, size)을 분리한다.
 * size 에는 다음 페이지 존재 여부(hasNext)를 판별하기 위해 요청 개수 + 1 이 담긴다.
 */
@Getter
@Setter
@Builder
public class TripSearchParam {

    /** 조회 기준 회원 ID (해당 회원이 참여한 여행만 조회) */
    private Long memberId;

    private String title;
    private Long tripRegionId;

    /** 여행 상태 (ETripStatus 이름) */
    private String status;

    private LocalDate dateFrom;
    private LocalDate dateTo;

    /** 무한 스크롤 커서 (이전 페이지 마지막 여행 ID) */
    private Long cursor;

    /** 조회 개수 (요청 size + 1) */
    private int size;
}
