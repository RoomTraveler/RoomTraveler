// import lombok.Getter;
// import lombok.Setter;

// @Getter
// @Setter
public class AccommodationSearchDto {
    // private String sidoCode; // 시도 코드
    // private String gugunCode; // 구군 코드
    // private String query; // 검색어 (숙소명, 지역명 등)
    // private String accommodationType; // 숙소 유형 (HOTEL, PENSION 등)
    // private Integer adults; // 성인 수 -> guests로 통합
    // private Integer children; // 아동 수 -> guests로 통합
    private Integer guests; // 총 인원 수
    private String checkInDate; // 체크인 날짜 (YYYY-MM-DD)
    private String checkOutDate; // 체크아웃 날짜 (YYYY-MM-DD)
    // private String sortBy; // 정렬 기준 (예: priceAsc, ratingDesc)
    // private Double userLatitude; // 사용자 현재 위도 (거리순 정렬 시)
    // private Double userLongitude; // 사용자 현재 경도 (거리순 정렬 시)

    // 기본 생성자
    public AccommodationSearchDto() {}

    // Getters and Setters for guests
    public Integer getGuests() {
        return guests;
    }

    public void setGuests(Integer guests) {
        this.guests = guests;
    }

    // 기존 adults, children 관련 getter/setter는 제거하거나 주석 처리
    /*
    public Integer getAdults() {
        return adults;
    }

    public void setAdults(Integer adults) {
        this.adults = adults;
    }

    public Integer getChildren() {
        return children;
    }

    public void setChildren(Integer children) {
        this.children = children;
    }
    */

    // 나머지 필드들의 Getter, Setter ...
} 