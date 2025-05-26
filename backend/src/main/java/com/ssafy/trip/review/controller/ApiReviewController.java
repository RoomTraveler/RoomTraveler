package com.ssafy.trip.review.controller;

import com.ssafy.trip.review.model.Review;
import com.ssafy.trip.review.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 리뷰 API 컨트롤러
 * 리뷰 관련 REST API 요청을 처리하며, 실제 비즈니스 로직은 ReviewService에 위임합니다.
 */
@RestController
@RequestMapping("/api/reviews")
public class ApiReviewController {

    private final ReviewService reviewService;

    /**
     * 생성자 주입을 통한 ReviewService 의존성 주입
     *
     * @param reviewService 리뷰 서비스
     */
    @Autowired
    public ApiReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * 숙소 ID로 해당 숙소의 모든 리뷰 목록을 조회합니다.
     * 각 리뷰에는 이미지 정보가 포함됩니다.
     *
     * @param accommodationId 숙소 ID
     * @return 성공 시 리뷰 목록과 HTTP 200 OK, 실패 시 오류 메시지와 HTTP 500 Internal Server Error
     */
    @GetMapping("/accommodation/{accommodationId}")
    public ResponseEntity<?> getReviewsByAccommodationId(@PathVariable Long accommodationId) {
        try {
            List<Review> reviews = reviewService.getReviewsByAccommodationId(accommodationId);
            return ResponseEntity.ok(reviews);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "리뷰 목록을 불러오는 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 특정 리뷰 ID로 리뷰 상세 정보를 조회합니다.
     * 리뷰에는 이미지 정보가 포함됩니다.
     *
     * @param reviewId 리뷰 ID
     * @return 성공 시 리뷰 정보와 HTTP 200 OK, 리뷰를 찾을 수 없을 경우 HTTP 404 Not Found, 실패 시 오류 메시지와 HTTP 500 Internal Server Error
     */
    @GetMapping("/{reviewId}")
    public ResponseEntity<?> getReviewById(@PathVariable Long reviewId) {
        try {
            Review review = reviewService.getReviewById(reviewId);
            if (review == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "리뷰를 찾을 수 없습니다: " + reviewId));
            }
            return ResponseEntity.ok(review);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "리뷰를 불러오는 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 특정 사용자 ID로 해당 사용자가 작성한 모든 리뷰 목록을 조회합니다.
     * 각 리뷰에는 이미지 정보가 포함됩니다.
     *
     * @param userId 사용자 ID
     * @return 성공 시 리뷰 목록과 HTTP 200 OK, 실패 시 오류 메시지와 HTTP 500 Internal Server Error
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getReviewsByUserId(@PathVariable Long userId) {
        try {
            List<Review> reviews = reviewService.getReviewsByUserId(userId);
            return ResponseEntity.ok(reviews);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "사용자 리뷰 목록을 불러오는 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 숙소 ID로 해당 숙소의 리뷰 요약 정보(평균 평점, 리뷰 개수 등)를 조회합니다.
     *
     * @param accommodationId 숙소 ID
     * @return 성공 시 리뷰 요약 정보와 HTTP 200 OK, 실패 시 오류 메시지와 HTTP 500 Internal Server Error
     */
    @GetMapping("/summary/accommodation/{accommodationId}")
    public ResponseEntity<?> getReviewSummaryByAccommodationId(@PathVariable Long accommodationId) {
        try {
            Map<String, Object> summary = reviewService.getReviewSummaryByAccommodationId(accommodationId);
            return ResponseEntity.ok(summary);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "리뷰 요약 정보를 불러오는 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 숙소 ID로 해당 숙소의 평점 분포(각 평점별 리뷰 개수)를 조회합니다.
     *
     * @param accommodationId 숙소 ID
     * @return 성공 시 평점 분포 정보와 HTTP 200 OK, 실패 시 오류 메시지와 HTTP 500 Internal Server Error
     */
    @GetMapping("/rating-distribution/accommodation/{accommodationId}")
    public ResponseEntity<?> getRatingDistributionByAccommodationId(@PathVariable Long accommodationId) {
        try {
            Map<Integer, Integer> distribution = reviewService.getRatingDistributionByAccommodationId(accommodationId);
            return ResponseEntity.ok(distribution);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "평점별 리뷰 개수를 불러오는 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 특정 예약 ID로 해당 예약에 작성된 리뷰를 조회합니다.
     * 리뷰에는 이미지 정보가 포함될 수 있습니다.
     *
     * @param reservationId 예약 ID
     * @return 성공 시 리뷰 정보와 HTTP 200 OK,
     *         리뷰를 찾을 수 없을 경우 HTTP 404 Not Found,
     *         실패 시 오류 메시지와 HTTP 500 Internal Server Error
     */
    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<?> getReviewByReservationId(@PathVariable Long reservationId) {
        try {
            // ReviewService에 getReviewByReservationId(Long reservationId) 메서드가 있다고 가정합니다.
            // 이 메서드는 해당 예약에 대한 리뷰가 있으면 Review 객체를, 없으면 null을 반환해야 합니다.
            Review review = reviewService.getReviewByReservationId(reservationId);
            if (review == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "해당 예약(" + reservationId + ")에 대한 리뷰를 찾을 수 없습니다."));
            }
            return ResponseEntity.ok(review);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "예약 ID로 리뷰를 불러오는 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 새 리뷰를 생성합니다.
     * 요청 본문에서 리뷰 정보(JSON)와 이미지 파일(Multipart)을 함께 받아 처리합니다.
     * 세션에서 사용자 ID를 가져와 서비스 계층에 전달합니다.
     *
     * @param reviewData 리뷰 데이터 (JSON 형식의 문자열을 Review 객체로 변환 필요)
     * @param images 첨부 이미지 파일 목록
     * @param captions 캡션 목록
     * @param session HTTP 세션 (사용자 ID를 가져오기 위해 사용)
     * @return 성공 시 생성된 리뷰 ID와 메시지 HTTP 201 Created,
     *         로그인이 필요할 경우 HTTP 401 Unauthorized,
     *         리뷰 작성 자격이 없을 경우 HTTP 403 Forbidden,
     *         실패 시 오류 메시지와 HTTP 500 Internal Server Error 또는 HTTP 400 Bad Request
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createReview(
            @RequestPart("review") Review reviewData,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "captions", required = false) List<String> captions,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "리뷰를 작성하려면 로그인이 필요합니다."));
        }

        if (images != null && captions != null && images.size() != captions.size()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "이미지와 캡션의 개수가 일치하지 않습니다."));
        }

        try {
            Long reviewId = reviewService.createReview(reviewData, userId, images, captions);
            Map<String, Object> response = new HashMap<>();
            response.put("reviewId", reviewId);
            response.put("message", "리뷰가 성공적으로 등록되었습니다.");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (SQLException e) {
            if (e.getMessage().contains("숙박한 기록이 없어")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "리뷰 저장 중 SQL 오류가 발생했습니다: " + e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "리뷰 이미지 처리 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 기존 리뷰를 업데이트합니다.
     * 요청 본문에서 수정할 리뷰 정보(JSON), 새로 추가할 이미지 파일, 삭제할 이미지 ID 목록을 받아 처리합니다.
     * 경로 변수에서 리뷰 ID, 세션에서 사용자 ID를 가져와 서비스 계층에 전달합니다.
     *
     * @param reviewId 업데이트할 리뷰의 ID
     * @param reviewData 수정된 리뷰 데이터 (JSON 형식)
     * @param newImages 새로 추가할 이미지 파일 목록
     * @param newCaptions 새로 추가할 이미지에 대한 캡션 목록
     * @param deleteImageIds 삭제할 기존 이미지의 ID 목록 (요청 파라미터)
     * @param session  HTTP 세션 (사용자 ID를 가져오기 위해 사용)
     * @return 성공 시 메시지와 HTTP 200 OK,
     *         로그인이 필요할 경우 HTTP 401 Unauthorized,
     *         리뷰를 찾을 수 없을 경우 HTTP 404 Not Found,
     *         수정 권한이 없을 경우 HTTP 403 Forbidden,
     *         실패 시 오류 메시지와 HTTP 500 Internal Server Error 또는 HTTP 400 Bad Request
     */
    @PutMapping(value = "/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateReview(
            @PathVariable Long reviewId,
            @RequestPart("review") Review reviewData,
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages,
            @RequestPart(value = "newCaptions", required = false) List<String> newCaptions,
            @RequestParam(value = "deleteImageIds", required = false) List<Long> deleteImageIds,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "리뷰를 수정하려면 로그인이 필요합니다."));
        }

        if (newImages != null && newCaptions != null && newImages.size() != newCaptions.size()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "새 이미지와 새 캡션의 개수가 일치하지 않습니다."));
        }

        try {
            reviewData.setReviewId(reviewId);
            boolean updated = reviewService.updateReview(reviewData, userId, newImages, newCaptions, deleteImageIds);
            if (updated) {
                return ResponseEntity.ok(Map.of("message", "리뷰가 성공적으로 수정되었습니다."));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "리뷰 수정에 실패했습니다. (일반 오류 또는 업데이트 대상 없음)"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (SQLException e) {
            if (e.getMessage().contains("찾을 수 없습니다")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            } else if (e.getMessage().contains("수정할 수 없습니다") || e.getMessage().contains("권한이 없습니다") || e.getMessage().contains("자신이 작성한 리뷰만")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "리뷰 수정 중 SQL 오류가 발생했습니다: " + e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "리뷰 이미지 처리 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 리뷰의 상태를 업데이트합니다. (예: ACTIVE, REPORTED, REMOVED 등)
     * 경로 변수에서 리뷰 ID, 요청 파라미터에서 새로운 상태, 세션에서 사용자 ID와 역할을 가져와 서비스 계층에 전달합니다.
     *
     * @param reviewId 업데이트할 리뷰의 ID
     * @param status   새로운 리뷰 상태
     * @param session  HTTP 세션 (사용자 ID 및 역할을 가져오기 위해 사용)
     * @return 성공 시 메시지와 HTTP 200 OK,
     *         로그인이 필요할 경우 HTTP 401 Unauthorized,
     *         리뷰를 찾을 수 없을 경우 HTTP 404 Not Found,
     *         상태 변경 권한이 없을 경우 HTTP 403 Forbidden,
     *         실패 시 오류 메시지와 HTTP 500 Internal Server Error
     */
    @PatchMapping("/{reviewId}/status")
    public ResponseEntity<?> updateReviewStatus(
            @PathVariable Long reviewId,
            @RequestParam String status,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        String userRole = (String) session.getAttribute("role");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "리뷰 상태를 변경하려면 로그인이 필요합니다."));
        }

        try {
            boolean updated = reviewService.updateReviewStatus(reviewId, status, userId, userRole);
            if (updated) {
                return ResponseEntity.ok(Map.of("message", "리뷰 상태가 성공적으로 변경되었습니다."));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "리뷰 상태 변경에 실패했습니다. (일반 오류)"));
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("찾을 수 없습니다")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            } else if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "리뷰 상태 변경 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 특정 리뷰를 삭제합니다.
     * 경로 변수에서 리뷰 ID를 받고, 세션에서 사용자 ID와 역할을 가져와 서비스 계층에 전달합니다.
     * 리뷰와 연결된 S3 이미지도 함께 삭제됩니다.
     *
     * @param reviewId 삭제할 리뷰의 ID
     * @param session  HTTP 세션 (사용자 ID 및 역할을 가져오기 위해 사용)
     * @return 성공 시 메시지와 HTTP 200 OK,
     *         로그인이 필요할 경우 HTTP 401 Unauthorized,
     *         리뷰를 찾을 수 없을 경우 HTTP 404 Not Found,
     *         삭제 권한이 없을 경우 HTTP 403 Forbidden,
     *         실패 시 오류 메시지와 HTTP 500 Internal Server Error
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @PathVariable Long reviewId,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        String userRole = (String) session.getAttribute("role");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "리뷰를 삭제하려면 로그인이 필요합니다."));
        }

        try {
            boolean deleted = reviewService.deleteReview(reviewId, userId, userRole);
            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "리뷰가 성공적으로 삭제되었습니다."));
            } else {
                // 이 경우는 ReviewServiceImpl에서 예외를 발생시키거나, 삭제된 행이 0개인 경우
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "리뷰 삭제에 실패했습니다. (일반 오류 또는 삭제 대상 없음)"));
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("찾을 수 없습니다")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            } else if (e.getMessage().contains("삭제할 수 없습니다") || e.getMessage().contains("권한이 없습니다") || e.getMessage().contains("자신이 작성한 리뷰만")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "리뷰 삭제 중 SQL 오류가 발생했습니다: " + e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "리뷰 이미지 삭제 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 특정 리뷰 이미지의 썸네일 상태를 설정합니다.
     * @param reviewId 리뷰 ID
     * @param imageId 썸네일로 지정할 이미지 ID
     * @param session HTTP 세션 (사용자 ID 확인용)
     * @return 성공 시 메시지와 HTTP 200 OK, 실패 시 오류 메시지 및 적절한 HTTP 상태 코드
     */
    @PatchMapping("/{reviewId}/images/{imageId}/thumbnail")
    public ResponseEntity<?> setReviewImageAsThumbnail(
            @PathVariable Long reviewId,
            @PathVariable Long imageId,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인이 필요합니다."));
        }
        try {
            boolean success = reviewService.setReviewImageThumbnail(reviewId, imageId, userId);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "이미지가 성공적으로 썸네일로 지정되었습니다."));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "썸네일 지정에 실패했습니다."));
        } catch (SQLException e) {
            if (e.getMessage().contains("찾을 수 없거나 권한이 없습니다") || e.getMessage().contains("해당 리뷰의 이미지를 찾을 수 없습니다")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "데이터베이스 오류: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "썸네일 지정 중 알 수 없는 오류 발생: " + e.getMessage()));
        }
    }

    /**
     * 특정 리뷰에 속한 이미지들의 표시 순서를 업데이트합니다.
     * @param reviewId 리뷰 ID
     * @param orderedImageIds 요청 본문에 정렬된 순서대로 이미지 ID 목록 (JSON 배열 형태)
     * @param session HTTP 세션 (사용자 ID 확인용)
     * @return 성공 시 메시지와 HTTP 200 OK, 실패 시 오류 메시지 및 적절한 HTTP 상태 코드
     */
    @PutMapping("/{reviewId}/images/order")
    public ResponseEntity<?> updateReviewImagesOrder(
            @PathVariable Long reviewId,
            @RequestBody List<Long> orderedImageIds,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인이 필요합니다."));
        }
        if (orderedImageIds == null || orderedImageIds.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "이미지 ID 목록을 제공해야 합니다."));
        }
        try {
            boolean success = reviewService.updateReviewImageOrder(reviewId, orderedImageIds, userId);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "이미지 순서가 성공적으로 업데이트되었습니다."));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "이미지 순서 업데이트에 실패했습니다."));
        } catch (SQLException e) {
             if (e.getMessage().contains("찾을 수 없거나 권한이 없습니다")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "데이터베이스 오류: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 특정 리뷰 이미지의 캡션을 업데이트합니다.
     * @param reviewId 리뷰 ID
     * @param imageId 캡션을 수정할 이미지 ID
     * @param captionData 요청 본문에 새로운 캡션 내용 (JSON 형태, 예: {"caption": "새로운 캡션"})
     * @param session HTTP 세션 (사용자 ID 확인용)
     * @return 성공 시 메시지와 HTTP 200 OK, 실패 시 오류 메시지 및 적절한 HTTP 상태 코드
     */
    @PatchMapping("/{reviewId}/images/{imageId}/caption")
    public ResponseEntity<?> updateImageCaption(
            @PathVariable Long reviewId,
            @PathVariable Long imageId,
            @RequestBody Map<String, String> captionData,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        String newCaption = captionData.get("caption");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인이 필요합니다."));
        }
        // 캡션 내용은 null일 수도, 빈 문자열일 수도 있으므로 별도 검증은 서비스 레이어에 맡기거나 필요시 추가
        try {
            boolean success = reviewService.updateReviewImageCaption(reviewId, imageId, newCaption, userId);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "이미지 캡션이 성공적으로 업데이트되었습니다."));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "이미지 캡션 업데이트에 실패했습니다."));
        } catch (SQLException e) {
            if (e.getMessage().contains("찾을 수 없거나 권한이 없습니다") || e.getMessage().contains("해당 리뷰의 이미지를 찾을 수 없습니다")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "데이터베이스 오류: " + e.getMessage()));
        }
    }

    /**
     * 호스트 ID로 해당 호스트가 관리하는 모든 숙소의 리뷰 목록을 조회합니다.
     * 각 리뷰에는 이미지 정보가 포함될 수 있습니다.
     * 선택적으로 rating으로 필터링할 수 있습니다.
     *
     * @param hostId 호스트 ID
     * @param rating 필터링할 별점 (선택 사항)
     * @return 성공 시 리뷰 목록과 HTTP 200 OK, 실패 시 오류 메시지와 HTTP 500 Internal Server Error
     */
    @GetMapping("/host/{hostId}")
    public ResponseEntity<?> getReviewsByHostId(
            @PathVariable Long hostId,
            @RequestParam(required = false) Integer rating) { // rating은 Integer로 받아 null 처리 용이
        try {
            List<Review> reviews = reviewService.getReviewsByHostId(hostId, rating);
            return ResponseEntity.ok(reviews);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "호스트의 리뷰 목록을 불러오는 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}