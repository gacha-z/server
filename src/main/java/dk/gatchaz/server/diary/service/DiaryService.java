package dk.gatchaz.server.diary.service;

import dk.gatchaz.server.diary.dto.DiaryCreateParam;
import dk.gatchaz.server.diary.dto.DiaryCreateRequest;
import dk.gatchaz.server.diary.dto.DiaryDetailResponse;
import dk.gatchaz.server.diary.dto.DiaryListResponse;
import dk.gatchaz.server.diary.dto.DiarySearchParam;
import dk.gatchaz.server.diary.dto.DiarySearchRequest;
import dk.gatchaz.server.diary.dto.DiaryGenerateRequest;
import dk.gatchaz.server.diary.dto.DiaryGenerateResponse;
import dk.gatchaz.server.diary.dto.DiaryTripContext;
import dk.gatchaz.server.diary.dto.DiaryUpdateRequest;
import dk.gatchaz.server.diary.mapper.DiaryMapper;
import dk.gatchaz.server.diary.support.DiaryContentGenerator;
import dk.gatchaz.server.exception.CommonException;
import dk.gatchaz.server.exception.ErrorCode;
import dk.gatchaz.server.notification.event.DiaryCreatedEvent;
import dk.gatchaz.server.type.EDiaryVisibility;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryMapper diaryMapper;
    private final DiaryContentGenerator diaryContentGenerator;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 일기를 저장한다. (본문 생성 로직 없음 — AI 생성은 generateDiary(/generate) 담당)
     * content(직접 작성 또는 /generate 로 받은 AI 초안)를 그대로 저장하며, isAiGenerated=true 면 'Y', 아니면 'N' 으로 기록한다.
     * trip_id / member_id 는 필수(요청값), status 는 'ACTIVE'(INSERT 쿼리), visibility 미지정 시 TEAM 으로 보정한다.
     */
    @Transactional
    public DiaryDetailResponse createDiary(final DiaryCreateRequest request) {
        // 저장 전용: content(직접 작성 또는 /generate 로 받은 AI 초안)를 그대로 저장한다.
        // AI 초안을 저장하는 경우 요청의 isAiGenerated=true 로 받아 'Y' 로 기록한다. (생성 로직은 /generate 담당)
        // 회원 기준 하루 1개 제한: 같은 회원이 같은 날짜에 (삭제되지 않은) 일기가 이미 있으면 실패.
        if (diaryMapper.countActiveDiaryByMemberAndDate(request.getMemberId(), request.getDiaryDate()) > 0) {
            throw new CommonException(ErrorCode.ALREADY_EXISTS_DIARY_DATE);
        }

        final EDiaryVisibility diaryVisibility =
                request.getVisibility() == null ? EDiaryVisibility.TEAM : request.getVisibility();
        final String visibility = diaryVisibility.name();
        final String isAiGenerated = Boolean.TRUE.equals(request.getIsAiGenerated()) ? "Y" : "N";

        final DiaryCreateParam param = DiaryCreateParam.builder()
                .tripId(request.getTripId())
                .memberId(request.getMemberId())
                .content(request.getContent())
                .diaryDate(request.getDiaryDate())
                .visibility(visibility)
                .isAiGenerated(isAiGenerated)
                .build();

        diaryMapper.insertDiary(param);

        // AI로 생성한 일기면 생성 이력(diary_ai_generation)도 함께 기록한다. (같은 트랜잭션)
        if ("Y".equals(isAiGenerated)) {
            diaryMapper.insertDiaryAiGeneration(
                    param.getDiaryId(), request.getSourceContent(), request.getContent());
        }

        // 같은 여행 팀원에게 알림을 보내기 위한 이벤트. 이 트랜잭션이 커밋된 뒤 별도 스레드에서 처리된다.
        // (저장이 롤백되면 알림도 나가지 않고, 알림 발송이 이 API 응답을 늦추지 않는다)
        eventPublisher.publishEvent(new DiaryCreatedEvent(
                param.getDiaryId(), request.getTripId(), request.getMemberId(), diaryVisibility));

        return diaryMapper.selectDiary(param.getDiaryId());
    }

    /**
     * 일기 내용(content)과 선택적 여행 컨텍스트로 AI가 일기 본문 초안을 생성해 반환한다. (저장하지 않음)
     * 느린 AI 호출 동안 DB 커넥션을 점유하지 않도록 트랜잭션으로 묶지 않는다.
     */
    public DiaryGenerateResponse generateDiary(final DiaryGenerateRequest request) {
        final DiaryTripContext context =
                request.getTripId() == null ? null : diaryMapper.selectTripContext(request.getTripId());
        final String content = diaryContentGenerator.generate(
                request.getContent(), request.getPromptOverride(), context);
        return new DiaryGenerateResponse(content, true);
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
                .diaryDate(request.getDiaryDate())
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
