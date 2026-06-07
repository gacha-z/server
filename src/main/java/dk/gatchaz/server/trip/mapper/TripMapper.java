package dk.gatchaz.server.trip.mapper;

import dk.gatchaz.server.trip.dto.TripRegionDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TripMapper {

    /**
     * 사용 가능한(use_yn = 'Y') 여행 지역 중 무작위로 cnt 개수만큼 조회한다.
     */
    List<TripRegionDto> selectRandomRegions(@Param("cnt") int cnt);
}
