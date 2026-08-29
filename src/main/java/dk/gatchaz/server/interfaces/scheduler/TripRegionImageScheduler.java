package dk.gatchaz.server.interfaces.scheduler;

import dk.gatchaz.server.interfaces.dto.TripRegionImageResponse;
import dk.gatchaz.server.interfaces.service.TripRegionImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 지역 대표 이미지(trip_region.image_url) 적재 배치.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TripRegionImageScheduler {

    private final TripRegionImageService tripRegionImageService;

    /**
     * 한 달에 한 번 모든 지역의 대표 이미지를 다시 조회해 갱신한다.
     * 실행 주기는 yml(batch.trip-region-image-cron)에서 관리하며, 값을 "-" 로 두면 배치가 실행되지 않는다.
     */
    @Scheduled(cron = "${batch.trip-region-image-cron}", zone = "Asia/Seoul")
    public void updateRegionImages() {
        log.info("지역 대표 이미지 배치 시작");
        try {
            final TripRegionImageResponse result = tripRegionImageService.updateRegionImages();
            log.info("지역 대표 이미지 배치 종료: 대상={}건, 성공={}건, 실패={}건",
                    result.getTotalCount(), result.getSuccessCount(), result.getFailCount());
        } catch (final Exception e) {
            log.error("지역 대표 이미지 배치 실패: {}", e.getMessage(), e);
        }
    }
}
