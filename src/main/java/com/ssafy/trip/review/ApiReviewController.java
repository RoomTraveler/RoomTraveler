package com.ssafy.trip.review;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 리뷰 API 컨트롤러
 * 리뷰 관련 REST API 요청을 처리합니다.
 */
@RestController
@RequestMapping("/api/reviews")
public class ApiReviewController {

    private final ReviewService reviewService;

    /**
     * 생성자 주입을 통한 의존성 주입
     */
    @Autowired
    public ApiReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * 숙소 ID로 리뷰 목록을 조회합니다.
     */
    @GetMapping("/accommodation/{accommodationId}")
    public ResponseEntity<?> getReviewsByAccommodationId(@PathVariable Long accommodationId) {
        try {
            List<Review> reviews = reviewService.getReviewsByAccommodationId(accommodationId);
            return ResponseEntity.ok(reviews);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("리뷰 목록을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 리뷰 ID로 리뷰를 조회합니다.
     */
    @GetMapping("/{reviewId}")
    public ResponseEntity<?> getReviewById(@PathVariable Long reviewId) {
        try {
            Review review = reviewService.getReviewById(reviewId);
            if (review == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("리뷰를 찾을 수 없습니다: " + reviewId);
            }
            return ResponseEntity.ok(review);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("리뷰를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 사용자 ID로 리뷰 목록을 조회합니다.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getReviewsByUserId(@PathVariable Long userId) {
        try {
            List<Review> reviews = reviewService.getReviewsByUserId(userId);
            return ResponseEntity.ok(reviews);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("리뷰 목록을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 숙소 ID로 리뷰 요약 정보를 조회합니다.
     */
    @GetMapping("/summary/accommodation/{accommodationId}")
    public ResponseEntity<?> getReviewSummaryByAccommodationId(@PathVariable Long accommodationId) {
        try {
            Map<String, Object> summary = reviewService.getReviewSummaryByAccommodationId(accommodationId);
            return ResponseEntity.ok(summary);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("리뷰 요약 정보를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 숙소 ID로 평점별 리뷰 개수를 조회합니다.
     */
    @GetMapping("/rating-distribution/accommodation/{accommodationId}")
    public ResponseEntity<?> getRatingDistributionByAccommodationId(@PathVariable Long accommodationId) {
        try {
            Map<Integer, Integer> distribution = reviewService.getRatingDistributionByAccommodationId(accommodationId);
            return ResponseEntity.ok(distribution);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("평점별 리뷰 개수를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 새 리뷰를 생성합니다.
     */
    @PostMapping
    public ResponseEntity<?> createReview(
            @RequestBody Review review,
            @RequestParam(required = false) Long userId) {
        
        // 사용자 ID가 없는 경우
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("리뷰를 작성하려면 로그인이 필요합니다.");
        }
        
        try {
            // 리뷰 작성 자격 확인
            boolean isEligible = reviewService.checkReviewEligibility(userId, review.getAccommodationId());
            
            if (!isEligible) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("해당 숙소에 숙박한 기록이 없어 리뷰를 작성할 수 없습니다.");
            }
            
            // 사용자 ID 설정
            review.setUserId(userId);
            
            // 기본값 설정
            review.setCreatedAt(LocalDateTime.now());
            review.setStatus("ACTIVE");
            review.setIsVerified(true); // 실제로는 예약 시스템과 연동하여 확인해야 함
            
            // 리뷰 저장
            Long reviewId = reviewService.createReview(review);
            
            Map<String, Object> response = new HashMap<>();
            response.put("reviewId", reviewId);
            response.put("message", "리뷰가 성공적으로 등록되었습니다.");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("리뷰 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 리뷰를 업데이트합니다.
     */
    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(
            @PathVariable Long reviewId,
            @RequestBody Review review,
            @RequestParam(required = false) Long userId) {
        
        // 사용자 ID가 없는 경우
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("리뷰를 수정하려면 로그인이 필요합니다.");
        }
        
        try {
            Review existingReview = reviewService.getReviewById(reviewId);
            
            // 리뷰가 존재하지 않는 경우
            if (existingReview == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("리뷰를 찾을 수 없습니다: " + reviewId);
            }
            
            // 리뷰 작성자가 아닌 경우
            if (!existingReview.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("자신이 작성한 리뷰만 수정할 수 있습니다.");
            }
            
            // 리뷰 ID 설정
            review.setReviewId(reviewId);
            
            // 기존 리뷰의 변경하지 않을 필드 유지
            review.setUserId(existingReview.getUserId());
            review.setAccommodationId(existingReview.getAccommodationId());
            review.setCreatedAt(existingReview.getCreatedAt());
            review.setIsVerified(existingReview.getIsVerified());
            review.setStatus(existingReview.getStatus());
            
            // 리뷰 업데이트
            boolean updated = reviewService.updateReview(review);
            
            if (updated) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "리뷰가 성공적으로 수정되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("리뷰 수정에 실패했습니다.");
            }
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("리뷰 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 리뷰 상태를 업데이트합니다.
     */
    @PatchMapping("/{reviewId}/status")
    public ResponseEntity<?> updateReviewStatus(
            @PathVariable Long reviewId,
            @RequestParam String status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String role) {
        
        // 사용자 ID가 없는 경우
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("리뷰 상태를 변경하려면 로그인이 필요합니다.");
        }
        
        try {
            Review review = reviewService.getReviewById(reviewId);
            
            // 리뷰가 존재하지 않는 경우
            if (review == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("리뷰를 찾을 수 없습니다: " + reviewId);
            }
            
            // 리뷰 작성자가 아니고 관리자도 아닌 경우
            if (!review.getUserId().equals(userId) && !"ADMIN".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("리뷰 상태를 변경할 권한이 없습니다.");
            }
            
            // 리뷰 상태 업데이트
            boolean updated = reviewService.updateReviewStatus(reviewId, status);
            
            if (updated) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "리뷰 상태가 성공적으로 변경되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("리뷰 상태 변경에 실패했습니다.");
            }
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("리뷰 상태 변경 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 리뷰를 삭제합니다.
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @PathVariable Long reviewId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String role) {
        
        // 사용자 ID가 없는 경우
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("리뷰를 삭제하려면 로그인이 필요합니다.");
        }
        
        try {
            Review review = reviewService.getReviewById(reviewId);
            
            // 리뷰가 존재하지 않는 경우
            if (review == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("리뷰를 찾을 수 없습니다: " + reviewId);
            }
            
            // 리뷰 작성자가 아니고 관리자도 아닌 경우
            if (!review.getUserId().equals(userId) && !"ADMIN".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("자신이 작성한 리뷰만 삭제할 수 있습니다.");
            }
            
            // 리뷰 삭제
            boolean deleted = reviewService.deleteReview(reviewId);
            
            if (deleted) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "리뷰가 성공적으로 삭제되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("리뷰 삭제에 실패했습니다.");
            }
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("리뷰 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}