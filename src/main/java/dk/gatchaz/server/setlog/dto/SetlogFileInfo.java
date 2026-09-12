package dk.gatchaz.server.setlog.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 셋로그 다운로드 시 소유권 확인용으로 조회하는 내부 정보 (API 응답 아님).
 */
@Getter
@Setter
@NoArgsConstructor
public class SetlogFileInfo {

    private Long memberId;

    private String fileUrl;
}
