package dk.gatchaz.server.interfaces.dto;

/**
 * TourAPI 지역기반 관광정보 조회(areaBasedList2) 응답 항목 중 지역 대표 이미지 선정에 필요한 값.
 * 응답 필드명은 모두 소문자다. (title, addr1, firstimage)
 *
 * @param title      관광지명 (선정 결과 로그 확인용)
 * @param addr1      주소 (선정 결과 로그 확인용)
 * @param firstImage 대표 이미지 URL. 값이 없으면 빈 문자열로 내려온다.
 */
public record TourAreaBasedItem(String title, String addr1, String firstImage) {
}
