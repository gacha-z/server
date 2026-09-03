package dk.gatchaz.server.interfaces.dto;

/**
 * TourAPI 법정동 코드 조회(ldongCode2) 응답 항목.
 *
 * @param code 법정동 코드. 시도 목록이면 시도 코드(예: 41 경기도, 36110 세종특별자치시),
 *             시군구 목록이면 시군구 코드(예: 110 수원시, 111 수원시 장안구)
 * @param name 지역명. 하위 일반구는 "수원시 장안구" 처럼 상위 시 이름을 앞에 붙여 내려온다.
 */
public record LdongCode(String code, String name) {
}
