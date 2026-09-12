package dk.gatchaz.server.trip.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class TripRegionDto {
    private Long tripCandidateId;
    private Long tripRegionId;
    private String tripRegionCode;
    private String tripRegionName;
    private String imageUrl;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String useYn;
    private LocalDateTime createdAt;
}
