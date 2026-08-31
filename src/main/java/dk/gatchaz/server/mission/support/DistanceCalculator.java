package dk.gatchaz.server.mission.support;

import java.math.BigDecimal;

/**
 * 두 좌표(위도/경도) 간의 거리를 계산한다. (Haversine 공식)
 */
public final class DistanceCalculator {

    private static final double EARTH_RADIUS_METER = 6_371_000.0;

    private DistanceCalculator() {
    }

    /**
     * 두 좌표 사이의 거리를 미터 단위로 계산한다.
     */
    public static double meters(final BigDecimal lat1, final BigDecimal lon1,
                                 final BigDecimal lat2, final BigDecimal lon2) {
        final double radLat1 = Math.toRadians(lat1.doubleValue());
        final double radLat2 = Math.toRadians(lat2.doubleValue());
        final double deltaLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        final double deltaLon = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());

        final double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METER * c;
    }
}
