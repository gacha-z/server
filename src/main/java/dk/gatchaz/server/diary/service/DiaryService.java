package dk.gatchaz.server.diary.service;

import dk.gatchaz.server.diary.dto.DiaryCreateParam;
import dk.gatchaz.server.diary.dto.DiaryCreateRequest;
import dk.gatchaz.server.diary.dto.DiaryCreateResponse;
import dk.gatchaz.server.diary.dto.DiaryDetailResponse;
import dk.gatchaz.server.diary.dto.DiaryListResponse;
import dk.gatchaz.server.diary.dto.DiarySearchParam;
import dk.gatchaz.server.diary.dto.DiarySearchRequest;
import dk.gatchaz.server.diary.dto.DiaryUpdateRequest;
import dk.gatchaz.server.diary.mapper.DiaryMapper;
import dk.gatchaz.server.exception.CommonException;
import dk.gatchaz.server.exception.ErrorCode;
import dk.gatchaz.server.type.EDiaryVisibility;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryMapper diaryMapper;

    /**
     * 일기를 생성한다.
     * trip_id / member_id 는 필수(요청값)이며, content 는 선택값으로 두어 추후 생성형 AI 로 본문을 채우는 확장에 대비한다.
     * status 는 'ACTIVE', is_ai_generated 는 'N' 으로 서버(INSERT 쿼리)가 지정하고, visibility 미지정 시 TEAM 으로 보정한다.
     */
    @Transactional
    public DiaryCreateResponse createDiary(final DiaryCreateRequest request) {
        final String visibility =
                (request.getVisibility() == null ? EDiaryVisibility.TEAM : request.getVisibility()).name();

        final DiaryCreateParam param = DiaryCreateParam.builder()
                .tripId(request.getTripId())
                .memberId(request.getMemberId())
                .content(request.getContent())
                .visibility(visibility)
                .build();

        diaryMapper.insertDiary(param);

        return new DiaryCreateResponse(param.getDiaryId());
    }

    /**
     * 일기 단건을 조회한다. 없거나 삭제된 일기면 예외를 던진다.
     */
    @Transactional(readOnly = true)
    public DiaryDetailResponse getDiary(final Long diaryId) {
        final DiaryDetailResponse diary = diaryMapper.selectDiary(diaryId);
        if (diary == null) {
            throw new CommonException(ErrorCode.NOT_FOUND_DIARY);
        }
        return diary;
    }

    /**
     * 조건(회원/여행)에 맞는 일기를 커서 기반 무한 스크롤로 조회한다.
     * hasNext 판별을 위해 요청 개수 + 1 을 조회한 뒤 초과분을 잘라낸다.
     */
    @Transactional(readOnly = true)
    public DiaryListResponse getDiaries(final DiarySearchRequest request) {
        // size 범위 보정 (1~50)
        final int size = Math.min(Math.max(request.getSize(), 1), 50);

        final DiarySearchParam param = DiarySearchParam.builder()
                .memberId(request.getMemberId())
                .tripId(request.getTripId())
                .cursor(request.getCursor())
                .size(size + 1) // 다음 페이지 존재 여부 판별용으로 1개 더 조회
                .build();

        final List<DiaryDetailResponse> diaries = diaryMapper.selectDiaries(param);

        final boolean hasNext = diaries.size() > size;
        final List<DiaryDetailResponse> pageDiaries = hasNext ? diaries.subList(0, size) : diaries;
        final Long nextCursor = hasNext ? pageDiaries.get(pageDiaries.size() - 1).getDiaryId() : null;

        return new DiaryListResponse(pageDiaries, nextCursor, hasNext);
    }

    /**
     * 일기(diaryId)의 본문/공개범위를 수정하고, 수정된 일기를 반환한다.
     * 대상이 없거나 이미 삭제된 일기면 예외를 던진다.
     */
    @Transactional
    public DiaryDetailResponse updateDiary(final Long diaryId, final DiaryUpdateRequest request) {
        // TODO: 로그인 연동 후 본인 일기만 수정 가능하도록 memberId 소유권 검증 추가
        final String visibility =
                (request.getVisibility() == null ? EDiaryVisibility.TEAM : request.getVisibility()).name();

        final int updated = diaryMapper.updateDiary(diaryId, request.getContent(), visibility);
        if (updated == 0) {
            throw new CommonException(ErrorCode.NOT_FOUND_DIARY);
        }
        return diaryMapper.selectDiary(diaryId);
    }

    /**
     * 일기(diaryId)를 소프트 삭제한다(status='DELETED'). 대상이 없거나 이미 삭제된 일기면 예외를 던진다.
     */
    @Transactional
    public void deleteDiary(final Long diaryId) {
        // TODO: 로그인 연동 후 본인 일기만 삭제 가능하도록 memberId 소유권 검증 추가
        final int deleted = diaryMapper.softDeleteDiary(diaryId);
        if (deleted == 0) {
            throw new CommonException(ErrorCode.NOT_FOUND_DIARY);
        }
    }
}
