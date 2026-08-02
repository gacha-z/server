package dk.gatchaz.server.diary.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 일기 저장(INSERT) 용 파라미터. INSERT 후 생성된 diaryId 가 채워진다.
 * status('ACTIVE')는 INSERT 쿼리에서 서버가 지정하고, is_ai_generated 는 서비스가 넣은 'Y'/'N' 값을 사용한다.
 */
@Getter
@Setter
@Builder
public class DiaryCreateParam {

    /** 생성된 일기 ID (INSERT 후 채워짐) */
    private Long diaryId;

    private Long memberId;
    private Long tripId;
    private String content;

    /** 일기 날짜 */
    private LocalDate diaryDate;

    /** 공개 범위 (EDiaryVisibility 이름, 미지정 시 서비스에서 TEAM 으로 보정) */
    private String visibility;

    /** AI 생성 여부 ('Y'/'N') */
    private String isAiGenerated;
}
