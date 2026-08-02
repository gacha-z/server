package dk.gatchaz.server.diary.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * AI 프롬프트 보강용 여행 컨텍스트. tripId 로 조회하며, 해당 여행이 없으면 null.
 */
@Getter
@Setter
@NoArgsConstructor
public class DiaryTripContext {

    /** 여행 제목 */
    private String title;

    /** 여행 지역명 (예: 강원특별자치도 강릉시). 지역 미선택이면 null */
    private String regionName;

    private LocalDate startDate;
    private LocalDate endDate;
}
