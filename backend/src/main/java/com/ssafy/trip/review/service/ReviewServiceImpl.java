package com.ssafy.trip.review.service;

import com.ssafy.trip.review.dao.ReviewDao;
import com.ssafy.trip.review.model.Review;
import com.ssafy.trip.review.model.ReviewImage;
import com.ssafy.trip.s3.AWSS3Service;
import com.ssafy.trip.accommodation.dao.ReservationDao;
import com.ssafy.trip.accommodation.dao.AccommodationDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 리뷰 서비스 구현 클래스
 * 리뷰 관련 비즈니스 로직을 처리합니다.
 */
@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewDao reviewDao;
    private final AWSS3Service awsS3Service; // S3 서비스 주입
    private final ReservationDao reservationDao; // ReservationDao 주입
    private final AccommodationDao accommodationDao; // AccommodationDao 주입

    @Value("${app.review.max-images-per-review:5}") // 기본값 5개
    private int maxImagesPerReview;

    /**
     * 생성자 주입을 통한 의존성 주입
     *
     * @param reviewDao 리뷰 데이터 접근 객체
     * @param awsS3Service S3 서비스
     * @param reservationDao ReservationDao
     * @param accommodationDao AccommodationDao
     */
    @Autowired
    public ReviewServiceImpl(ReviewDao reviewDao, AWSS3Service awsS3Service, ReservationDao reservationDao, AccommodationDao accommodationDao) {
        this.reviewDao = reviewDao;
        this.awsS3Service = awsS3Service;
        this.reservationDao = reservationDao;
        this.accommodationDao = accommodationDao;
    }

    /**
     * 새 리뷰를 생성합니다.
     * 사용자 ID를 리뷰 정보에 설정하고, 기본값(생성 시간, 상태)을 설정한 후 저장합니다.
     * 리뷰 작성 자격 여부를 확인합니다.
     * 첨부된 이미지 파일들을 S3에 업로드하고, 해당 URL들을 `review_images` 테이블에 저장합니다.
     *
     * @param review 생성할 리뷰 정보
     * @param userId 작성자 ID
     * @param imageFiles 첨부된 이미지 파일 목록
     * @param captions 이미지 캡션 목록
     * @return 생성된 리뷰 ID
     * @throws SQLException 데이터베이스 오류 발생 시 또는 리뷰 작성 자격이 없는 경우
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    @Override
    @Transactional
    public Long createReview(Review review, Long userId, List<MultipartFile> imageFiles, List<String> captions) throws SQLException, IOException {
        if (!checkReviewEligibility(userId, review.getAccommodationId())) {
            throw new SQLException("해당 숙소에 숙박한 기록이 없어 리뷰를 작성할 수 없습니다.");
        }

        // 이미지 개수 제한 검사
        if (imageFiles != null && imageFiles.size() > maxImagesPerReview) {
            throw new IllegalArgumentException("리뷰에는 최대 " + maxImagesPerReview + "개의 이미지만 첨부할 수 있습니다.");
        }

        review.setUserId(userId);
        review.setCreatedAt(LocalDateTime.now());
        review.setStatus(review.getStatus() == null ? "ACTIVE" : review.getStatus());
        review.setIsVerified(review.getIsVerified() == null ? true : review.getIsVerified());

        reviewDao.insert(review);
        Long reviewId = review.getReviewId();
        Long accommodationId = review.getAccommodationId(); // 숙소 ID 가져오기

        if (imageFiles != null && !imageFiles.isEmpty()) {
            List<ReviewImage> reviewImages = new ArrayList<>();
            for (int i = 0; i < imageFiles.size(); i++) {
                MultipartFile file = imageFiles.get(i);
                String caption = (captions != null && i < captions.size()) ? captions.get(i) : null;
                if (file != null && !file.isEmpty()) {
                    String imageUrl = awsS3Service.uploadFile(file); // AWSS3Service에서 크기/형식 검사
                    ReviewImage reviewImage = ReviewImage.builder()
                            .reviewId(reviewId)
                            .imageUrl(imageUrl)
                            .isThumbnail(i == 0) // 첫 번째 이미지를 썸네일로 자동 지정
                            .sortOrder(i)
                            .caption(caption) // 캡션 설정
                            .build();
                    reviewImages.add(reviewImage);
                }
            }
            if (!reviewImages.isEmpty()) {
                reviewDao.insertReviewImages(reviewImages);
            }
        }
        
        // 숙소 리뷰 통계 업데이트
        updateAccommodationReviewStats(accommodationId);
        
        return reviewId;
    }

    /**
     * 리뷰 ID로 리뷰를 조회합니다.
     * MyBatis collection 매핑을 통해 이미지 정보가 자동으로 포함됩니다.
     *
     * @param reviewId 조회할 리뷰 ID
     * @return 조회된 리뷰 정보. 해당 ID의 리뷰가 없으면 null 반환.
     * @throws SQLException 데이터베이스 오류 발생 시
     */
    @Override
    public Review getReviewById(Long reviewId) throws SQLException {
        return reviewDao.selectById(reviewId);
    }

    /**
     * 숙소 ID로 리뷰 목록을 조회합니다.
     * MyBatis collection 매핑을 통해 이미지 정보가 자동으로 포함됩니다.
     *
     * @param accommodationId 조회할 숙소 ID
     * @return 해당 숙소의 리뷰 목록
     * @throws SQLException 데이터베이스 오류 발생 시
     */
    @Override
    public List<Review> getReviewsByAccommodationId(Long accommodationId) throws SQLException {
        return reviewDao.selectByAccommodationId(accommodationId);
    }

    /**
     * 사용자 ID로 리뷰 목록을 조회합니다.
     * MyBatis collection 매핑을 통해 이미지 정보가 자동으로 포함됩니다.
     *
     * @param userId 조회할 사용자 ID
     * @return 해당 사용자가 작성한 리뷰 목록
     * @throws SQLException 데이터베이스 오류 발생 시
     */
    @Override
    public List<Review> getReviewsByUserId(Long userId) throws SQLException {
        return reviewDao.selectByUserId(userId);
    }

    /**
     * 숙소 ID로 리뷰 평균 평점을 조회합니다.
     *
     * @param accommodationId 조회할 숙소 ID
     * @return 해당 숙소의 평균 평점. 리뷰가 없으면 0.0 반환.
     * @throws SQLException 데이터베이스 오류 발생 시
     */
    @Override
    public Double getAverageRatingByAccommodationId(Long accommodationId) throws SQLException {
        Double averageRating = reviewDao.selectAverageRatingByAccommodationId(accommodationId);
        return averageRating != null ? averageRating : 0.0;
    }

    /**
     * 숙소 ID로 리뷰 개수를 조회합니다.
     *
     * @param accommodationId 조회할 숙소 ID
     * @return 해당 숙소의 리뷰 개수
     * @throws SQLException 데이터베이스 오류 발생 시
     */
    @Override
    public Integer getReviewCountByAccommodationId(Long accommodationId) throws SQLException {
        return reviewDao.selectCountByAccommodationId(accommodationId);
    }

    /**
     * 리뷰를 업데이트합니다.
     * 리뷰 작성자만 해당 리뷰를 수정할 수 있습니다. 업데이트 시 수정 시간을 현재 시간으로 설정합니다.
     * 기존 이미지를 삭제하거나 새 이미지를 추가할 수 있습니다.
     *
     * @param review 업데이트할 리뷰 정보 (텍스트)
     * @param userId 수정 요청 사용자 ID
     * @param newImageFiles 새로 추가할 이미지 파일 목록
     * @param newCaptions 새로 추가할 이미지 캡션 목록
     * @param deleteImageIds 삭제할 기존 이미지의 ID 목록
     * @return 업데이트 성공 시 true, 실패 시 false
     * @throws SQLException 데이터베이스 오류 발생 시 또는 리뷰를 찾을 수 없거나 수정 권한이 없는 경우
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    @Override
    @Transactional
    public boolean updateReview(Review review, Long userId, List<MultipartFile> newImageFiles, List<String> newCaptions, List<Long> deleteImageIds) throws SQLException, IOException {
        Review existingReview = reviewDao.selectById(review.getReviewId());
        if (existingReview == null) {
            throw new SQLException("리뷰를 찾을 수 없습니다: " + review.getReviewId());
        }
        if (!existingReview.getUserId().equals(userId)) {
            throw new SQLException("자신이 작성한 리뷰만 수정할 수 있습니다.");
        }
        Long accommodationId = existingReview.getAccommodationId(); // 숙소 ID 가져오기

        // 이미지 개수 검증 (기존 이미지 + 새 이미지 - 삭제될 이미지)
        List<ReviewImage> currentImages = reviewDao.selectReviewImagesByReviewId(review.getReviewId());
        int currentImageCount = currentImages != null ? currentImages.size() : 0;
        int newImageCount = newImageFiles != null ? newImageFiles.size() : 0;
        int deleteImageCount = deleteImageIds != null ? deleteImageIds.size() : 0;
        if (currentImageCount + newImageCount - deleteImageCount > maxImagesPerReview) {
            throw new IllegalArgumentException("리뷰에는 최대 " + maxImagesPerReview + "개의 이미지만 첨부할 수 있습니다.");
        }

        review.setUserId(userId); // 변경될 수 없지만, 명시적으로 설정
        review.setUpdatedAt(LocalDateTime.now()); // 수정 시간 업데이트
        review.setStatus(existingReview.getStatus()); // 상태는 이 메서드에서 변경하지 않음
        review.setIsVerified(existingReview.getIsVerified()); // 검증 상태는 이 메서드에서 변경하지 않음
        review.setCreatedAt(existingReview.getCreatedAt()); // 생성 시간은 유지
        review.setAccommodationId(accommodationId); // 숙소 ID는 변경 불가

        int updatedRows = reviewDao.update(review); // 리뷰 텍스트 정보 업데이트

        // 기존 이미지 삭제 처리
        if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
            for (Long imageIdToDelete : deleteImageIds) {
                ReviewImage imageToDelete = reviewDao.selectReviewImageById(imageIdToDelete);
                if (imageToDelete != null && imageToDelete.getReviewId().equals(review.getReviewId())) {
                    awsS3Service.deleteImage(imageToDelete.getImageUrl()); // S3에서 이미지 삭제
                    reviewDao.deleteReviewImageById(imageIdToDelete); // DB에서 이미지 정보 삭제
                }
            }
        }

        // 새 이미지 추가 처리
        if (newImageFiles != null && !newImageFiles.isEmpty()) {
            List<ReviewImage> newReviewImages = new ArrayList<>();
            // 새 이미지 추가 시 현재 이미지들의 최대 sortOrder 다음부터 시작
            int maxSortOrder = -1;
            List<ReviewImage> remainingImages = reviewDao.selectReviewImagesByReviewId(review.getReviewId());
            if (remainingImages != null) {
                for (ReviewImage img : remainingImages) {
                    if (img.getSortOrder() > maxSortOrder) {
                        maxSortOrder = img.getSortOrder();
                    }
                }
            }

            for (int i = 0; i < newImageFiles.size(); i++) {
                MultipartFile file = newImageFiles.get(i);
                String caption = (newCaptions != null && i < newCaptions.size()) ? newCaptions.get(i) : null;
                if (file != null && !file.isEmpty()) {
                    String imageUrl = awsS3Service.uploadFile(file);
                    ReviewImage reviewImage = ReviewImage.builder()
                            .reviewId(review.getReviewId())
                            .imageUrl(imageUrl)
                            .isThumbnail(false) // 썸네일은 API로 명시적으로 지정, 기본값 false
                            .sortOrder(maxSortOrder + 1 + i) // 순서 할당
                            .caption(caption)
                            .build();
                    newReviewImages.add(reviewImage);
                }
            }
            if (!newReviewImages.isEmpty()) {
                reviewDao.insertReviewImages(newReviewImages);
            }
        }

        // 썸네일 재설정 로직 (필요 시)
        // 만약 이미지 삭제/추가 후 썸네일이 없어졌다면, 남은 이미지 중 첫 번째를 썸네일로 설정
        List<ReviewImage> finalImages = reviewDao.selectReviewImagesByReviewId(review.getReviewId());
        if (finalImages != null && !finalImages.isEmpty()) {
            boolean hasThumbnail = finalImages.stream().anyMatch(img -> img.getIsThumbnail() != null && img.getIsThumbnail());
            if (!hasThumbnail) {
                reviewDao.resetThumbnailStatusByReviewId(review.getReviewId()); // 모든 썸네일 false로
                reviewDao.updateReviewImageThumbnailStatus(finalImages.get(0).getImageId(), true); // 첫 이미지 썸네일로
            }
        } else {
             // 이미지가 모두 삭제된 경우 처리 (필요하다면)
        }
        
        // 숙소 리뷰 통계 업데이트
        if (updatedRows > 0) { // 리뷰 텍스트가 실제로 업데이트 되었을 때만 통계 업데이트 (선택적)
            updateAccommodationReviewStats(accommodationId);
        }
        
        return updatedRows > 0;
    }

    /**
     * 리뷰 상태를 업데이트합니다.
     * 리뷰 작성자 또는 관리자('ADMIN' 역할)만 상태를 변경할 수 있습니다.
     *
     * @param reviewId 상태를 변경할 리뷰 ID
     * @param status 새로운 상태
     * @param userId 상태 변경 요청 사용자 ID
     * @param userRole 상태 변경 요청 사용자의 역할 (예: "ADMIN")
     * @return 업데이트 성공 시 true, 실패 시 false
     * @throws SQLException 데이터베이스 오류 발생 시 또는 리뷰를 찾을 수 없거나 상태 변경 권한이 없는 경우
     */
    @Override
    @Transactional
    public boolean updateReviewStatus(Long reviewId, String status, Long userId, String userRole) throws SQLException {
        Review review = reviewDao.selectById(reviewId);
        if (review == null) {
            throw new SQLException("리뷰를 찾을 수 없습니다: " + reviewId);
        }
        // 리뷰 작성자이거나 ADMIN 역할일 경우에만 상태 변경 가능
        if (!review.getUserId().equals(userId) && !"ADMIN".equalsIgnoreCase(userRole)) {
            throw new SQLException("리뷰 상태를 변경할 권한이 없습니다.");
        }
        return reviewDao.updateStatus(reviewId, status) > 0;
    }

    /**
     * 리뷰를 삭제합니다.
     * 리뷰 작성자 또는 관리자('ADMIN' 역할)만 리뷰를 삭제할 수 있습니다.
     * 리뷰와 관련된 모든 이미지도 S3 및 DB에서 삭제됩니다.
     *
     * @param reviewId 삭제할 리뷰 ID
     * @param userId 삭제 요청 사용자 ID
     * @param userRole 삭제 요청 사용자의 역할 (예: "ADMIN")
     * @return 삭제 성공 시 true, 실패 시 false
     * @throws SQLException 데이터베이스 오류 발생 시 또는 리뷰를 찾을 수 없거나 삭제 권한이 없는 경우
     * @throws IOException S3 이미지 삭제 중 오류 발생 시
     */
    @Override
    @Transactional
    public boolean deleteReview(Long reviewId, Long userId, String userRole) throws SQLException, IOException {
        Review review = reviewDao.selectById(reviewId);
        if (review == null) {
            throw new SQLException("리뷰를 찾을 수 없습니다: " + reviewId);
        }

        // 관리자 또는 리뷰 작성자만 삭제 가능
        if (!("ADMIN".equals(userRole) || review.getUserId().equals(userId))) {
            throw new SQLException("리뷰를 삭제할 권한이 없습니다.");
        }
        
        Long accommodationId = review.getAccommodationId(); // 숙소 ID 가져오기

        // 연결된 이미지들 S3에서 삭제 및 DB에서 삭제
        List<ReviewImage> images = reviewDao.selectReviewImagesByReviewId(reviewId);
        if (images != null && !images.isEmpty()) {
            for (ReviewImage image : images) {
                awsS3Service.deleteImage(image.getImageUrl()); // S3에서 이미지 파일 삭제
            }
            reviewDao.deleteReviewImagesByReviewId(reviewId); // DB에서 모든 이미지 정보 삭제
        }

        int deletedRows = reviewDao.delete(reviewId); // 리뷰 삭제

        // 숙소 리뷰 통계 업데이트
        if (deletedRows > 0) {
            updateAccommodationReviewStats(accommodationId);
        }

        return deletedRows > 0;
    }

    /**
     * 숙소 ID로 리뷰 요약 정보를 조회합니다.
     * (예: 평균 평점, 리뷰 개수 등)
     *
     * @param accommodationId 조회할 숙소 ID
     * @return 리뷰 요약 정보
     * @throws SQLException 데이터베이스 오류 발생 시
     */
    @Override
    public Map<String, Object> getReviewSummaryByAccommodationId(Long accommodationId) throws SQLException {
        return reviewDao.selectReviewSummaryByAccommodationId(accommodationId);
    }

    /**
     * 최근 리뷰 목록을 조회합니다.
     * MyBatis collection 매핑을 통해 이미지 정보가 자동으로 포함됩니다.
     *
     * @param limit 조회할 최근 리뷰의 개수
     * @return 최근 리뷰 목록
     * @throws SQLException 데이터베이스 오류 발생 시
     */
    @Override
    public List<Review> getRecentReviews(int limit) throws SQLException {
        return reviewDao.selectRecentReviews(limit);
    }

    /**
     * 평점별 리뷰 개수를 조회합니다.
     *
     * @param accommodationId 조회할 숙소 ID
     * @return 평점(키)과 해당 평점의 리뷰 개수(값)를 담은 Map
     * @throws SQLException 데이터베이스 오류 발생 시
     */
    @Override
    public Map<Integer, Integer> getRatingDistributionByAccommodationId(Long accommodationId) throws SQLException {
        return reviewDao.selectRatingDistributionByAccommodationId(accommodationId);
    }

    /**
     * 리뷰 작성 자격을 확인합니다.
     * 사용자가 해당 숙소에 실제로 숙박했는지 (또는 예약 기록이 있는지 등) 확인합니다.
     * <p>
     * 참고: 이 메서드는 예약 시스템과 연동하여 정확한 자격 여부를 판단해야 합니다.
     * 현재 구현은 임시적으로 항상 true를 반환합니다.
     *
     * @param userId 확인할 사용자 ID
     * @param accommodationId 확인할 숙소 ID
     * @return 리뷰 작성 자격이 있으면 true, 없으면 false (현재는 항상 true)
     * @throws SQLException 데이터베이스 오류 발생 시
     */
    @Override
    public boolean checkReviewEligibility(Long userId, Long accommodationId) throws SQLException {
        if (userId == null || accommodationId == null) {
            return false; // 유효하지 않은 입력
        }
        // ReservationDao를 사용하여 사용자가 해당 숙소에 완료된 예약이 있는지 확인
        return reservationDao.existsCompletedReservationByUserAndAccommodation(userId, accommodationId);
    }

    /**
     * 예약 ID로 리뷰를 조회합니다.
     * MyBatis collection 매핑을 통해 이미지 정보가 자동으로 포함됩니다.
     *
     * @param reservationId 조회할 예약 ID
     * @return 해당 예약에 대한 리뷰 정보. 리뷰가 없으면 null 반환.
     * @throws SQLException 데이터베이스 오류 발생 시
     */
    @Override
    public Review getReviewByReservationId(Long reservationId) throws SQLException {
        return reviewDao.selectByReservationId(reservationId);
    }

    // --- 요청하신 추가 기능 메서드들 --- 

    @Override
    @Transactional
    public boolean setReviewImageThumbnail(Long reviewId, Long imageId, Long userId) throws SQLException {
        Review review = reviewDao.selectById(reviewId);
        if (review == null || !review.getUserId().equals(userId)) {
            throw new SQLException("리뷰 정보를 찾을 수 없거나 권한이 없습니다.");
        }
        ReviewImage image = reviewDao.selectReviewImageById(imageId);
        if (image == null || !image.getReviewId().equals(reviewId)) {
            throw new SQLException("해당 리뷰의 이미지를 찾을 수 없습니다.");
        }

        reviewDao.resetThumbnailStatusByReviewId(reviewId); // 기존 썸네일 해제
        return reviewDao.updateReviewImageThumbnailStatus(imageId, true) > 0;
    }

    @Override
    @Transactional
    public boolean updateReviewImageOrder(Long reviewId, List<Long> orderedImageIds, Long userId) throws SQLException {
        Review review = reviewDao.selectById(reviewId);
        if (review == null || !review.getUserId().equals(userId)) {
            throw new SQLException("리뷰 정보를 찾을 수 없거나 권한이 없습니다.");
        }
        
        List<ReviewImage> currentImages = reviewDao.selectReviewImagesByReviewId(reviewId);
        if (currentImages == null || currentImages.size() != orderedImageIds.size()) {
            throw new IllegalArgumentException("제공된 이미지 ID 목록이 현재 리뷰의 이미지 수와 일치하지 않습니다.");
        }
        // 모든 ID가 실제로 해당 리뷰에 속하는지 확인 (선택적이지만 안전함)
        for (Long imgId : orderedImageIds) {
            if (currentImages.stream().noneMatch(ci -> ci.getImageId().equals(imgId))) {
                throw new IllegalArgumentException("잘못된 이미지 ID가 포함되어 있습니다: " + imgId);
            }
        }

        int updatedCount = 0;
        for (int i = 0; i < orderedImageIds.size(); i++) {
            updatedCount += reviewDao.updateReviewImageSortOrder(orderedImageIds.get(i), i);
        }
        return updatedCount == orderedImageIds.size();
    }

    @Override
    @Transactional
    public boolean updateReviewImageCaption(Long reviewId, Long imageId, String caption, Long userId) throws SQLException {
        Review review = reviewDao.selectById(reviewId);
        if (review == null || !review.getUserId().equals(userId)) {
            throw new SQLException("리뷰 정보를 찾을 수 없거나 권한이 없습니다.");
        }
        ReviewImage image = reviewDao.selectReviewImageById(imageId);
        if (image == null || !image.getReviewId().equals(reviewId)) {
            throw new SQLException("해당 리뷰의 이미지를 찾을 수 없습니다.");
        }
        return reviewDao.updateReviewImageCaption(imageId, caption) > 0;
    }

    // Helper method to update accommodation review statistics
    private void updateAccommodationReviewStats(Long accommodationId) throws SQLException {
        if (accommodationId == null) {
            // log.warn("Accommodation ID is null. Cannot update review stats.");
            return;
        }
        Map<String, Object> stats = reviewDao.selectReviewStatsByAccommodationId(accommodationId);
        Double avgRating = 0.0;
        Integer reviewCount = 0;

        if (stats != null) {
            Object rawAvgRating = stats.get("avg_rating");
            if (rawAvgRating instanceof Number) {
                avgRating = ((Number) rawAvgRating).doubleValue();
            }
            
            Object rawReviewCount = stats.get("review_count");
            if (rawReviewCount instanceof Number) {
                reviewCount = ((Number) rawReviewCount).intValue();
            }
        }

        // 추천 점수 계산: avg_review_rating * LOG10(review_count + 1)
        // 사용자가 SQL에서 LOG가 상용로그라고 언급했으므로 Math.log10 사용
        double recommendScore = 0.0;
        if (reviewCount > 0) { 
            recommendScore = avgRating * Math.log10(reviewCount + 1);
        } else {
            // reviewCount가 0이면 reviewCount + 1 = 1, log10(1) = 0 이므로 recommendScore는 0이 됨.
            // avgRating이 0이거나 reviewCount가 0이면 recommendScore는 자연스럽게 0이 됨.
        }
        
        // 소수점 처리 (예: 둘째 자리까지 반올림)
        recommendScore = Math.round(recommendScore * 100.0) / 100.0;
        // avgRating은 DB에서 가져올 때 이미 DECIMAL(3,2) 등으로 처리될 수 있으나, 여기서 한 번 더 명시적으로 처리
        avgRating = Math.round(avgRating * 100.0) / 100.0;

        accommodationDao.updateReviewStats(accommodationId, avgRating, reviewCount, recommendScore);
        // log.info("Updated review stats for accommodation {}: avgRating={}, reviewCount={}, recommendScore={}", 
        // accommodationId, avgRating, reviewCount, recommendScore);
    }
}
