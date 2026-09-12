package dk.gatchaz.server.diary.mapper;

import dk.gatchaz.server.diary.dto.DiaryCreateParam;
import dk.gatchaz.server.diary.dto.DiaryDetailResponse;
import dk.gatchaz.server.diary.dto.DiarySearchParam;
import dk.gatchaz.server.diary.dto.DiarySummaryResponse;
import dk.gatchaz.server.diary.dto.DiaryTripContext;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
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

    /**
     * AI 프롬프트 보강용 여행 컨텍스트(제목/지역명/기간)를 조회한다. 해당 여행이 없으면 null.
     */
    DiaryTripContext selectTripContext(@Param("tripId") Long tripId);

    /**
     * AI 생성 이력(diary_ai_generation)을 1건 저장한다. (selected_yn='Y')
     * AI로 생성해 저장한 일기(diaryId)에만 기록한다.
     */
    int insertDiaryAiGeneration(@Param("diaryId") Long diaryId,
                                @Param("sourceContent") String sourceContent,
                                @Param("generatedContent") String generatedContent);

    /**
     * 해당 회원(memberId)이 그 날짜(diaryDate)에 작성한, 삭제되지 않은 일기 수를 센다. (회원별 하루 1개 제한 검증용)
     */
    int countActiveDiaryByMemberAndDate(@Param("memberId") Long memberId, @Param("diaryDate") LocalDate diaryDate);

    /**
     * 해당 여행(tripId) 참여자 전체가 특정 날짜(diaryDate)에 쓴 일기 요약 목록을 조회한다. (삭제된 일기는 제외)
     * 본문/공개범위/AI 생성여부/상태는 포함하지 않는다 - 필요하면 단건 조회(selectDiary)로 확인한다.
     */
    List<DiarySummaryResponse> selectTripDiariesByDate(@Param("tripId") Long tripId,
                                                        @Param("diaryDate") LocalDate diaryDate);
}
