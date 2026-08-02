package dk.gatchaz.server.diary.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 일기 생성(INSERT) 용 파라미터. INSERT 후 생성된 diaryId 가 채워진다.
 * status('ACTIVE') / is_ai_generated('N') 는 INSERT 쿼리에서 서버가 직접 지정한다.
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

    /** 공개 범위 (EDiaryVisibility 이름, 미지정 시 서비스에서 TEAM 으로 보정) */
    private String visibility;
}
