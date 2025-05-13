package com.ssafy.trip.accommodation.controller;

import com.ssafy.trip.accommodation.model.Accommodation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 숙소 API 컨트롤러
 * 숙소 정보에 대한 RESTful API 엔드포인트를 제공합니다.
 */
@RestController
@RequestMapping("/api/accommodations")
public class ApiAccommodationController {

    /**
     * 모든 숙소 목록을 조회합니다.
     * GET /api/accommodations
     */
    @GetMapping
    public ResponseEntity<List<Accommodation>> getAllAccommodations(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false, defaultValue = "recommended") String sort) {
        
        // 실제 구현에서는 데이터베이스에서 조회
        // 현재는 샘플 데이터 반환
        List<Accommodation> accommodations = getSampleAccommodations();
        
        // 필터링 로직 (실제 구현에서는 데이터베이스 쿼리로 처리)
        if (location != null && !location.isEmpty()) {
            accommodations.removeIf(acc -> !acc.getLocation().contains(location));
        }
        
        if (minPrice != null) {
            accommodations.removeIf(acc -> acc.getPrice() < minPrice);
        }
        
        if (maxPrice != null) {
            accommodations.removeIf(acc -> acc.getPrice() > maxPrice);
        }
        
        // 정렬 로직 (실제 구현에서는 데이터베이스 쿼리로 처리)
        switch (sort) {
            case "price-low":
                accommodations.sort((a, b) -> a.getPrice() - b.getPrice());
                break;
            case "price-high":
                accommodations.sort((a, b) -> b.getPrice() - a.getPrice());
                break;
            case "rating":
                accommodations.sort((a, b) -> Double.compare(b.getRating(), a.getRating()));
                break;
            case "recommended":
            default:
                // 기본 정렬은 추천순 (평점 * 리뷰 수)
                accommodations.sort((a, b) -> 
                    Double.compare(b.getRating() * b.getReviewCount(), a.getRating() * a.getReviewCount()));
                break;
        }
        
        return ResponseEntity.ok(accommodations);
    }

    /**
     * 특정 ID의 숙소를 조회합니다.
     * GET /api/accommodations/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Accommodation> getAccommodationById(@PathVariable Long id) {
        // 실제 구현에서는 데이터베이스에서 조회
        // 현재는 샘플 데이터에서 ID로 찾기
        List<Accommodation> accommodations = getSampleAccommodations();
        Accommodation accommodation = accommodations.stream()
                .filter(acc -> acc.getId().equals(id))
                .findFirst()
                .orElse(null);
        
        if (accommodation == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(accommodation);
    }

    /**
     * 새로운 숙소를 등록합니다.
     * POST /api/accommodations
     */
    @PostMapping
    public ResponseEntity<Accommodation> createAccommodation(@RequestBody Accommodation accommodation) {
        // 실제 구현에서는 데이터베이스에 저장
        // 현재는 ID 할당 후 반환
        accommodation.setId(System.currentTimeMillis());
        accommodation.setCreatedAt(java.time.LocalDateTime.now().toString());
        accommodation.setUpdatedAt(java.time.LocalDateTime.now().toString());
        
        return ResponseEntity.ok(accommodation);
    }

    /**
     * 특정 ID의 숙소 정보를 수정합니다.
     * PUT /api/accommodations/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Accommodation> updateAccommodation(
            @PathVariable Long id, 
            @RequestBody Accommodation accommodation) {
        
        // 실제 구현에서는 데이터베이스에서 조회 후 업데이트
        // 현재는 ID 확인 후 업데이트된 객체 반환
        List<Accommodation> accommodations = getSampleAccommodations();
        boolean exists = accommodations.stream()
                .anyMatch(acc -> acc.getId().equals(id));
        
        if (!exists) {
            return ResponseEntity.notFound().build();
        }
        
        accommodation.setId(id);
        accommodation.setUpdatedAt(java.time.LocalDateTime.now().toString());
        
        return ResponseEntity.ok(accommodation);
    }

    /**
     * 특정 ID의 숙소를 삭제합니다.
     * DELETE /api/accommodations/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccommodation(@PathVariable Long id) {
        // 실제 구현에서는 데이터베이스에서 삭제
        // 현재는 성공 응답만 반환
        return ResponseEntity.ok().build();
    }

    /**
     * 샘플 숙소 데이터를 생성합니다.
     */
    private List<Accommodation> getSampleAccommodations() {
        List<Accommodation> accommodations = new ArrayList<>();
        
        accommodations.add(Accommodation.builder()
                .id(1L)
                .name("서울 시티 호텔")
                .location("서울 강남구")
                .address("서울특별시 강남구 테헤란로 123")
                .price(120000)
                .rating(4.5)
                .reviewCount(32)
                .description("강남역 인근에 위치한 현대적인 호텔입니다. 비즈니스 및 관광에 최적의 위치를 자랑합니다.")
                .imageUrl("https://via.placeholder.com/300x200?text=서울+시티+호텔")
                .amenities(Arrays.asList("무료 Wi-Fi", "에어컨", "헤어 드라이어", "냉장고", "전자레인지", "욕조", "주차장"))
                .hostId("host1")
                .hostName("김호스트")
                .isActive(true)
                .createdAt("2023-01-01T00:00:00")
                .updatedAt("2023-01-01T00:00:00")
                .build());
        
        accommodations.add(Accommodation.builder()
                .id(2L)
                .name("부산 오션 리조트")
                .location("부산 해운대구")
                .address("부산광역시 해운대구 해운대해변로 123")
                .price(150000)
                .rating(4.7)
                .reviewCount(48)
                .description("해운대 해변이 보이는 아름다운 리조트입니다. 바다 전망과 함께 편안한 휴식을 즐기세요.")
                .imageUrl("https://via.placeholder.com/300x200?text=부산+오션+리조트")
                .amenities(Arrays.asList("무료 Wi-Fi", "에어컨", "헤어 드라이어", "냉장고", "수영장", "헬스장", "주차장"))
                .hostId("host2")
                .hostName("이호스트")
                .isActive(true)
                .createdAt("2023-02-01T00:00:00")
                .updatedAt("2023-02-01T00:00:00")
                .build());
        
        accommodations.add(Accommodation.builder()
                .id(3L)
                .name("제주 풀빌라")
                .location("제주 서귀포시")
                .address("제주특별자치도 서귀포시 중문관광로 123")
                .price(200000)
                .rating(4.9)
                .reviewCount(56)
                .description("제주의 아름다운 자연 속에 위치한 프라이빗 풀빌라입니다. 완벽한 휴식을 위한 최고의 선택입니다.")
                .imageUrl("https://via.placeholder.com/300x200?text=제주+풀빌라")
                .amenities(Arrays.asList("무료 Wi-Fi", "에어컨", "헤어 드라이어", "냉장고", "개인 수영장", "바베큐", "주차장"))
                .hostId("host3")
                .hostName("박호스트")
                .isActive(true)
                .createdAt("2023-03-01T00:00:00")
                .updatedAt("2023-03-01T00:00:00")
                .build());
        
        return accommodations;
    }
}