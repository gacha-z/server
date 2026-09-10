package dk.gatchaz.server.collection.mapper;

import dk.gatchaz.server.collection.dto.BadgeMeta;
import dk.gatchaz.server.collection.dto.BadgeResponse;
import dk.gatchaz.server.collection.dto.CollectionItemResponse;
import dk.gatchaz.server.collection.dto.MemberBadgeProgress;
import dk.gatchaz.server.collection.dto.MemberDiaryCount;
import dk.gatchaz.server.collection.dto.RegionVisitCount;
import dk.gatchaz.server.collection.dto.TripRegionGroupInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CollectionMapper {

    /**
     * 전체 아이템 목록을 회원 기준 보유 여부와 함께 조회한다.
     */
    List<CollectionItemResponse> selectItems(@Param("memberId") Long memberId);

    /**
     * 전체 배지 목록을 회원 기준 진행률/달성 여부와 함께 조회한다.
     */
    List<BadgeResponse> selectBadges(@Param("memberId") Long memberId);

    // ------------------------------------------------------------------
    // 배지/아이템 지급 처리용 (이벤트 리스너에서만 사용)
    // ------------------------------------------------------------------

    /**
     * 지역 그룹에 대응하는 아이템 ID를 조회한다. 그룹당 아이템이 정확히 1개라고 가정한다.
     */
    Long selectCollectionItemIdByRegionGroup(@Param("regionGroupId") Long regionGroupId);

    int existsMemberItem(@Param("memberId") Long memberId, @Param("collectionItemId") Long collectionItemId);

    void insertMemberItem(
            @Param("memberId") Long memberId,
            @Param("collectionItemId") Long collectionItemId,
            @Param("tripId") Long tripId);

    BadgeMeta selectBadgeMetaByCode(@Param("badgeCode") String badgeCode);

    MemberBadgeProgress selectMemberBadgeProgress(@Param("memberId") Long memberId, @Param("badgeId") Long badgeId);

    void insertMemberBadgeProgress(
            @Param("memberId") Long memberId,
            @Param("badgeId") Long badgeId,
            @Param("currentCount") int currentCount,
            @Param("achievedYn") String achievedYn,
            @Param("achievedAt") LocalDateTime achievedAt);

    void updateMemberBadgeProgress(
            @Param("memberId") Long memberId,
            @Param("badgeId") Long badgeId,
            @Param("currentCount") int currentCount,
            @Param("achievedYn") String achievedYn,
            @Param("achievedAt") LocalDateTime achievedAt);

    /**
     * 여행에 현재 참여(JOINED) 중인 회원 ID 목록. 미션 완료 시 팀 전원에게 배지 진행도를 올려주기 위해 쓴다.
     */
    List<Long> selectJoinedMemberIds(@Param("tripId") Long tripId);

    /**
     * 여행(tripId)이 선택한 지역이 속한 지역 그룹 정보를 조회한다. (여행 완료 시 지역별 배지/아이템 지급용)
     * 지역이 선택되지 않았거나 지역 그룹이 매핑되지 않았으면 null.
     */
    TripRegionGroupInfo selectRegionGroupInfoByTrip(@Param("tripId") Long tripId);

    /**
     * 회원(memberId)이 완료(status = 'COMPLETED')한 여행들을 지역 그룹 기준으로 묶어 각각 몇 번 방문했는지 조회한다.
     * 지역 탐험 배지(서로 다른 지역 3회 / 동일 지역 3회) 판단에 쓰인다.
     */
    List<RegionVisitCount> selectRegionVisitCounts(@Param("memberId") Long memberId);

    // ------------------------------------------------------------------
    // 여행 취소 시 배지 진행도 되돌리기용 (이벤트 리스너에서만 사용)
    // ------------------------------------------------------------------

    /**
     * 여행(tripId)에서 완료(status = 'COMPLETED')된 미션 개수를 조회한다. (전체 유형)
     * 취소된 여행에서 이 개수만큼 FIRST_MISSION/MISSION_EXPERT_5 진행도를 되돌려야 한다.
     */
    int countCompletedMissionsByTrip(@Param("tripId") Long tripId);

    /**
     * 여행(tripId)에서 완료(status = 'COMPLETED')된 특정 유형(missionType)의 미션 개수를 조회한다.
     * 취소된 여행에서 이 개수만큼 FOOD/CAFE 관련 배지 진행도를 되돌려야 한다.
     */
    int countCompletedMissionsByTripAndType(@Param("tripId") Long tripId, @Param("missionType") String missionType);

    /**
     * 여행(tripId)에서 회원별로 작성한 일기 개수를 조회한다. (소프트 삭제 여부와 무관하게 작성 시점에 배지가
     * 올라갔으므로 전부 센다) 취소된 여행에서 회원별로 이 개수만큼 FIRST_DIARY/DIARY_10 진행도를 되돌려야 한다.
     */
    List<MemberDiaryCount> selectDiaryCountsByTrip(@Param("tripId") Long tripId);
}
