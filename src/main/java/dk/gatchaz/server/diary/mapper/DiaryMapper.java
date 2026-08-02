package dk.gatchaz.server.diary.mapper;

import dk.gatchaz.server.diary.dto.DiaryCreateParam;
import dk.gatchaz.server.diary.dto.DiaryDetailResponse;
import dk.gatchaz.server.diary.dto.DiarySearchParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiaryMapper {

    /**
     * 일기를 새로 생성한다. 생성된 diary_id 는 param.diaryId 에 채워진다.
     */
    int insertDiary(DiaryCreateParam param);

    /**
     * 일기 단건을 조회한다. 삭제된(status='DELETED') 일기는 제외하며, 없으면 null 을 반환한다.
     */
    DiaryDetailResponse selectDiary(@Param("diaryId") Long diaryId);

    /**
     * 조건(회원/여행)에 맞는 일기를 커서 기준(diary_id 내림차순) 최신순으로 size 개수만큼 조회한다.
     * hasNext 판별을 위해 서비스에서 요청 개수 + 1 을 size 로 넘긴다. 삭제된(status='DELETED') 일기는 제외한다.
     */
    List<DiaryDetailResponse> selectDiaries(DiarySearchParam param);

    /**
     * 일기(diaryId)의 본문/공개범위를 수정한다. 삭제되지 않은 일기만 갱신하며, 대상이 없으면 0 을 반환한다.
     */
    int updateDiary(@Param("diaryId") Long diaryId,
                    @Param("content") String content,
                    @Param("visibility") String visibility);

    /**
     * 일기(diaryId)를 소프트 삭제한다. (status → 'DELETED') 이미 삭제되었거나 없으면 0 을 반환한다.
     */
    int softDeleteDiary(@Param("diaryId") Long diaryId);
}
