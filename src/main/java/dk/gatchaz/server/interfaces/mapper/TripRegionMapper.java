package dk.gatchaz.server.interfaces.mapper;

import dk.gatchaz.server.interfaces.dto.TripRegionImageTarget;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TripRegionMapper {

    /**
     * 대표 이미지 적재 대상 지역을 조회한다.
     * 사용 중(use_yn = 'Y')이고 법정동 코드(trip_region_code)가 있는 지역만 대상으로 한다.
     */
    List<TripRegionImageTarget> selectImageTargetRegions();

    /**
     * 지역(tripRegionId)의 대표 이미지 URL 을 갱신한다.
     */
    int updateImageUrl(@Param("tripRegionId") Long tripRegionId, @Param("imageUrl") String imageUrl);
}
