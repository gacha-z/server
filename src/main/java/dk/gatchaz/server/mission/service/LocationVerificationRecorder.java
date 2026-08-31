package dk.gatchaz.server.mission.service;

import dk.gatchaz.server.mission.mapper.MissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 위치 인증 시도 이력을 별도 트랜잭션(REQUIRES_NEW)으로 기록한다.
 * 완료 처리 트랜잭션이 인증 실패로 롤백되더라도, 실패 이력 자체는 남아야 하기 때문에
 * 호출한 트랜잭션과 분리해서 즉시 커밋한다.
 */
@Service
@RequiredArgsConstructor
public class LocationVerificationRecorder {

    private final MissionMapper missionMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(final Long tripId,
                        final Long tripMissionId,
                        final Long memberId,
                        final BigDecimal latitude,
                        final BigDecimal longitude,
                        final BigDecimal distanceMeter,
                        final boolean success) {
        missionMapper.insertLocationVerificationLog(
                tripId, tripMissionId, memberId, latitude, longitude, distanceMeter, success ? "Y" : "N");
    }
}
