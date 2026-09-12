package dk.gatchaz.server.setlog.service;

import dk.gatchaz.server.common.exception.CommonException;
import dk.gatchaz.server.common.exception.ErrorCode;
import dk.gatchaz.server.setlog.dto.SetlogDownloadResponse;
import dk.gatchaz.server.setlog.dto.SetlogFileInfo;
import dk.gatchaz.server.setlog.dto.SetlogInsertParam;
import dk.gatchaz.server.setlog.dto.SetlogResponse;
import dk.gatchaz.server.setlog.dto.SetlogUploadResponse;
import dk.gatchaz.server.setlog.mapper.SetlogMapper;
import dk.gatchaz.server.setlog.support.S3Uploader;
import dk.gatchaz.server.type.ESetlogStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SetlogService {

    private final SetlogMapper setlogMapper;
    private final S3Uploader s3Uploader;

    /**
     * 셋로그(영상)를 업로드한다. 하나의 트랜잭션으로 처리된다.
     * 1. 진행 중(IN_PROGRESS)인 미션인지 확인한다.
     * 2. 참여(JOINED) 중인 팀원인지 확인한다.
     * 3. 이미 이 미션에 셋로그를 등록했으면(재업로드) 실패한다.
     * 4. S3 에 업로드하고, 렌더링 위치(slot_no)를 서버가 자동 배정해 저장한다.
     */
    @Transactional
    public SetlogUploadResponse uploadSetlog(final Long tripId, final Long tripMissionId, final Long userId,
                                              final MultipartFile file) {
        // 1. 진행 중인 미션인지 확인
        if (setlogMapper.existsInProgressTripMission(tripId, tripMissionId) == 0) {
            throw new CommonException(ErrorCode.NOT_FOUND_TRIP_MISSION);
        }

        // 2. 참여 중인 팀원인지 확인
        if (setlogMapper.existsJoinedMember(tripId, userId) == 0) {
            throw new CommonException(ErrorCode.NOT_FOUND_TRIP_MEMBER);
        }

        // 3. 재업로드 방지 (이 미션에 이미 셋로그를 등록했으면 실패)
        if (setlogMapper.existsSetlogByMember(tripMissionId, userId) > 0) {
            throw new CommonException(ErrorCode.ALREADY_EXISTS_SETLOG);
        }

        // 4. S3 업로드 (허용되지 않은 확장자면 S3Uploader 가 예외를 던진다)
        final String fileUrl = s3Uploader.upload(file, tripId, tripMissionId);

        // 5. 렌더링 위치(slot_no) 자동 배정 후 저장
        final int slotNo = setlogMapper.countSetlogsForMission(tripMissionId) + 1;
        final SetlogInsertParam param = SetlogInsertParam.builder()
                .tripId(tripId)
                .tripMissionId(tripMissionId)
                .memberId(userId)
                .fileUrl(fileUrl)
                .slotNo(slotNo)
                .status(ESetlogStatus.ACTIVE.name())
                .build();
        setlogMapper.insertSetlog(param);

        return new SetlogUploadResponse(param.getSetlogId(), tripMissionId, userId, fileUrl, slotNo, LocalDateTime.now());
    }

    /**
     * 여행(tripId)의 전체 셋로그 목록을 조회한다. 요청자가 그 여행 참여자가 아니면 예외를 던진다.
     */
    @Transactional(readOnly = true)
    public List<SetlogResponse> getSetlogsByTrip(final Long tripId, final Long userId) {
        if (setlogMapper.existsJoinedMember(tripId, userId) == 0) {
            throw new CommonException(ErrorCode.NOT_FOUND_TRIP_MEMBER);
        }
        return setlogMapper.selectSetlogsByTrip(tripId);
    }

    /**
     * 특정 진행 미션(tripMissionId)의 셋로그 목록을 조회한다. (미션 완료 검증용)
     * 요청자가 그 여행 참여자가 아니면 예외를 던진다.
     */
    @Transactional(readOnly = true)
    public List<SetlogResponse> getSetlogsByMission(final Long tripMissionId, final Long userId) {
        requireTripMissionMember(tripMissionId, userId);
        return setlogMapper.selectSetlogsByMission(tripMissionId);
    }

    /**
     * 본인이 촬영한 셋로그(setlogId)를 다운로드한다. 다운로드 시도 이력을 남기고 영상 URL 을 반환한다.
     * 없으면 404(NOT_FOUND_SETLOG), 본인이 촬영한 것이 아니면 403(NOT_SETLOG_OWNER)을 던진다.
     */
    @Transactional
    public SetlogDownloadResponse downloadSetlog(final Long setlogId, final Long userId) {
        final SetlogFileInfo info = setlogMapper.selectSetlogFileInfo(setlogId);
        if (info == null) {
            throw new CommonException(ErrorCode.NOT_FOUND_SETLOG);
        }
        if (!info.getMemberId().equals(userId)) {
            throw new CommonException(ErrorCode.NOT_SETLOG_OWNER);
        }
        setlogMapper.insertSetlogDownloadLog(setlogId, userId, "SUCCESS");
        return new SetlogDownloadResponse(info.getFileUrl());
    }

    /**
     * 같은 여행 참여자(본인 포함)가 진행 미션(tripMissionId)에 등록된 팀원 전체의 셋로그를 한 번에 다운로드한다.
     * 반환하는 각 셋로그마다 다운로드 시도 이력을 남긴다. 요청자가 그 여행 참여자가 아니면 예외를 던진다.
     */
    @Transactional
    public List<SetlogResponse> downloadSetlogsByMission(final Long tripMissionId, final Long userId) {
        requireTripMissionMember(tripMissionId, userId);

        final List<SetlogResponse> setlogs = setlogMapper.selectSetlogsByMission(tripMissionId);
        for (final SetlogResponse setlog : setlogs) {
            setlogMapper.insertSetlogDownloadLog(setlog.getSetlogId(), userId, "SUCCESS");
        }
        return setlogs;
    }

    /**
     * 요청자(userId)가 해당 진행 미션(tripMissionId)이 속한 여행의 참여자인지 확인한다.
     * 진행 미션이 없으면 404(NOT_FOUND_TRIP_MISSION), 참여자가 아니면 404(NOT_FOUND_TRIP_MEMBER)를 던진다.
     */
    private void requireTripMissionMember(final Long tripMissionId, final Long userId) {
        final Long tripId = setlogMapper.selectTripIdByTripMission(tripMissionId);
        if (tripId == null) {
            throw new CommonException(ErrorCode.NOT_FOUND_TRIP_MISSION);
        }
        if (setlogMapper.existsJoinedMember(tripId, userId) == 0) {
            throw new CommonException(ErrorCode.NOT_FOUND_TRIP_MEMBER);
        }
    }
}
