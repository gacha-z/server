package dk.gatchaz.server.setlog.mapper;

import dk.gatchaz.server.setlog.dto.SetlogInsertParam;
import dk.gatchaz.server.setlog.dto.SetlogResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SetlogMapper {

    /**
     * 여행(tripId)에서 진행 중(status = 'IN_PROGRESS')인 특정 trip_mission(tripMissionId)이 존재하는지 확인한다.
     * (존재하면 1 이상)
     */
    int existsInProgressTripMission(@Param("tripId") Long tripId, @Param("tripMissionId") Long tripMissionId);

    /**
     * 회원(memberId)이 해당 여행(tripId)에 현재 참여(JOINED) 중인지 확인한다. (참여 중이면 1 이상)
     */
    int existsJoinedMember(@Param("tripId") Long tripId, @Param("memberId") Long memberId);

    /**
     * 회원(memberId)이 이 진행 미션(tripMissionId)에 이미 셋로그를 등록했는지 확인한다. (있으면 1 이상, 재업로드 불가 가드)
     */
    int existsSetlogByMember(@Param("tripMissionId") Long tripMissionId, @Param("memberId") Long memberId);

    /**
     * 이 진행 미션(tripMissionId)에 등록된 셋로그 개수를 조회한다. (다음 slot_no = 이 값 + 1)
     */
    int countSetlogsForMission(@Param("tripMissionId") Long tripMissionId);

    /**
     * 셋로그를 저장한다. 생성된 setlog_id 는 param.setlogId 에 채워진다.
     */
    int insertSetlog(SetlogInsertParam param);

    /**
     * 여행(tripId)의 전체 셋로그 목록을 조회한다. (진행 미션 순 -> 슬롯 순)
     */
    List<SetlogResponse> selectSetlogsByTrip(@Param("tripId") Long tripId);

    /**
     * 특정 진행 미션(tripMissionId)의 셋로그 목록을 슬롯 순으로 조회한다. (미션 완료 검증용)
     */
    List<SetlogResponse> selectSetlogsByMission(@Param("tripMissionId") Long tripMissionId);

    /**
     * 셋로그(setlogId)의 영상 URL을 조회한다. 없거나 삭제된 셋로그면 null.
     */
    String selectFileUrlById(@Param("setlogId") Long setlogId);

    /**
     * 다운로드 시도 이력을 setlog_download_log 에 기록한다.
     */
    int insertSetlogDownloadLog(@Param("setlogId") Long setlogId,
                                 @Param("memberId") Long memberId,
                                 @Param("status") String status);
}
