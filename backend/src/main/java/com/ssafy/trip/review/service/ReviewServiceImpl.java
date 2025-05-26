package com.ssafy.trip.review.service;

import com.ssafy.trip.accommodation.dao.AccommodationDao;
import com.ssafy.trip.accommodation.model.Accommodation;
import com.ssafy.trip.exception.ResourceNotFoundException;
import com.ssafy.trip.exception.UnauthorizedException;
import com.ssafy.trip.review.dao.ReviewDao;
import com.ssafy.trip.review.dto.ReviewCreationRequestDto;
import com.ssafy.trip.review.dto.ReviewResponseDto;
import com.ssafy.trip.review.dto.ReviewUpdateRequestDto;
import com.ssafy.trip.review.model.Review;
import com.ssafy.trip.review.model.ReviewImage;
import com.ssafy.trip.s3.AWSS3Service;
import com.ssafy.trip.user.UserDao;
import com.ssafy.trip.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 리뷰 서비스 구현 클래스
 * 리뷰 관련 비즈니스 로직을 처리합니다.
 */
@Service
public class ReviewServiceImpl implements ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ReviewDao reviewDao;
    private final AWSS3Service awsS3Service;
    private final UserDao userDao;
    private final AccommodationDao accommodationDao;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${app.review.max-images-per-review:5}")
    private int maxImagesPerReview;

    public ReviewServiceImpl(ReviewDao reviewDao, AWSS3Service awsS3Service, UserDao userDao, AccommodationDao accommodationDao) {
        this.reviewDao = reviewDao;
        this.awsS3Service = awsS3Service;
        this.userDao = userDao;
        this.accommodationDao = accommodationDao;
    }

    @Override
    public boolean canUserReviewAccommodation(Long userId, Long accommodationId) throws SQLException {
        if (userId == null || accommodationId == null) {
            return false;
        }
        return reviewDao.checkUserReservationForAccommodation(userId, accommodationId);
    }

    @Override
    @Transactional
    public ReviewResponseDto createReview(ReviewCreationRequestDto requestDto, List<MultipartFile> imageFiles, Long userId)
            throws SQLException, IOException, UnauthorizedException, ResourceNotFoundException {
        
        User user = userDao.selectUserById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + userId);
        }

        if (!canUserReviewAccommodation(userId, requestDto.getAccommodationId())) {
            throw new UnauthorizedException("이 숙소에 대한 리뷰를 작성할 권한이 없습니다. 먼저 예약을 완료해주세요.");
        }
        
        if (requestDto.getReservationId() != null) {
            Review existingReviewForReservation = reviewDao.selectReviewByReservationId(requestDto.getReservationId());
            if (existingReviewForReservation != null) {
                throw new IllegalStateException("이미 이 예약에 대한 리뷰가 존재합니다.");
            }
        }

        if (!CollectionUtils.isEmpty(imageFiles) && imageFiles.size() > maxImagesPerReview) {
            throw new IllegalArgumentException("리뷰에는 최대 " + maxImagesPerReview + "개의 이미지만 첨부할 수 있습니다.");
        }
        
        Review review = Review.builder()
                .accommodationId(requestDto.getAccommodationId())
                .userId(userId)
                .reservationId(requestDto.getReservationId())
                .rating(requestDto.getRating())
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .status("ACTIVE") 
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        reviewDao.insertReview(review); 

        List<ReviewImage> reviewImages = new ArrayList<>();
        if (!CollectionUtils.isEmpty(imageFiles)) {
            for (int i = 0; i < imageFiles.size(); i++) {
                MultipartFile file = imageFiles.get(i);
                if (file != null && !file.isEmpty()) {
                    String imageUrl = awsS3Service.uploadFile(file); 

                    ReviewImage reviewImage = ReviewImage.builder()
                            .reviewId(review.getReviewId())
                            .imageUrl(imageUrl)
                            .uploadOrder(i)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    reviewDao.insertReviewImage(reviewImage);
                    reviewImages.add(reviewImage);
                }
            }
        }
        review.setImages(reviewImages);

        return convertToDto(review);
    }

    @Override
    @Transactional
    public ReviewResponseDto updateReview(Long reviewId, ReviewUpdateRequestDto requestDto, List<MultipartFile> newImageFiles, Long userId)
            throws SQLException, IOException, ResourceNotFoundException, UnauthorizedException {
        
        Review review = reviewDao.selectReviewById(reviewId); 
        if (review == null) {
            throw new ResourceNotFoundException("리뷰를 찾을 수 없습니다: " + reviewId);
        }
        if (!review.getUserId().equals(userId)) {
            throw new UnauthorizedException("이 리뷰를 수정할 권한이 없습니다.");
        }
        
        List<ReviewImage> existingImages = review.getImages() == null ? new ArrayList<>() : review.getImages();
        int currentImageCount = existingImages.size();
        int newImageInputCount = !CollectionUtils.isEmpty(newImageFiles) ? newImageFiles.size() : 0;
        int deletedImageCount = !CollectionUtils.isEmpty(requestDto.getDeletedImageIds()) ? requestDto.getDeletedImageIds().size() : 0;

        if (currentImageCount - deletedImageCount + newImageInputCount > maxImagesPerReview) {
            throw new IllegalArgumentException("리뷰에는 최대 " + maxImagesPerReview + "개의 이미지만 첨부할 수 있습니다.");
        }

        review.setRating(requestDto.getRating());
        review.setTitle(requestDto.getTitle());
        review.setContent(requestDto.getContent());
        review.setUpdatedAt(LocalDateTime.now());
        reviewDao.updateReview(review); 

        if (!CollectionUtils.isEmpty(requestDto.getDeletedImageIds())) {
            for (Long imageIdToDelete : requestDto.getDeletedImageIds()) {
                ReviewImage imageToDelete = existingImages.stream()
                                                .filter(img -> img.getReviewImageId().equals(imageIdToDelete))
                                                .findFirst().orElse(null);
                if (imageToDelete != null) {
                    try {
                        String s3Key = extractS3KeyFromUrl(imageToDelete.getImageUrl());
                        if (s3Key != null) awsS3Service.deleteImage(s3Key); 
                    } catch (Exception e) {
                        logger.error("S3 이미지 삭제 중 오류 발생 (Image ID: {}): {}", imageIdToDelete, e.getMessage());
                    }
                    reviewDao.deleteReviewImageById(imageIdToDelete); 
                }
            }
        }
        
        Review reviewAfterImageDeletion = reviewDao.selectReviewById(reviewId);
        List<ReviewImage> currentReviewImagesAfterDelete = reviewAfterImageDeletion.getImages() == null ? new ArrayList<>() : reviewAfterImageDeletion.getImages();

        if (!CollectionUtils.isEmpty(newImageFiles)) {
            int maxOrder = currentReviewImagesAfterDelete.stream()
                                .mapToInt(ReviewImage::getUploadOrder)
                                .max().orElse(-1);

            for (int i = 0; i < newImageFiles.size(); i++) {
                MultipartFile file = newImageFiles.get(i);
                if (file != null && !file.isEmpty()) {
                    String imageUrl = awsS3Service.uploadFile(file);
                    ReviewImage reviewImage = ReviewImage.builder()
                            .reviewId(review.getReviewId())
                            .imageUrl(imageUrl)
                            .uploadOrder(maxOrder + 1 + i)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    reviewDao.insertReviewImage(reviewImage);
                }
            }
        }
        
        Review updatedReview = reviewDao.selectReviewById(reviewId);
        return convertToDto(updatedReview);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, Long userId) throws SQLException, IOException, ResourceNotFoundException, UnauthorizedException {
        Review review = reviewDao.selectReviewById(reviewId);
        if (review == null) {
            throw new ResourceNotFoundException("리뷰를 찾을 수 없습니다: " + reviewId);
        }
        if (!review.getUserId().equals(userId)) {
            throw new UnauthorizedException("이 리뷰를 삭제할 권한이 없습니다.");
        }

        if (!CollectionUtils.isEmpty(review.getImages())) {
            for (ReviewImage image : review.getImages()) {
                try {
                    String s3Key = extractS3KeyFromUrl(image.getImageUrl());
                    if (s3Key != null) awsS3Service.deleteImage(s3Key);
                } catch (Exception e) {
                    logger.error("S3 이미지 삭제 중 오류 발생 (리뷰 ID: {}, 이미지 URL: {}): {}", reviewId, image.getImageUrl(), e.getMessage());
                }
            }
        }
        
        reviewDao.deleteReviewImagesByReviewId(reviewId); 
        reviewDao.deleteReviewById(reviewId, userId);
    }

    @Override
    public Page<ReviewResponseDto> getReviewsByAccommodationId(Long accommodationId, Pageable pageable) throws SQLException {
        List<Review> allReviews = reviewDao.selectReviewsByAccommodationId(accommodationId); 
        if (CollectionUtils.isEmpty(allReviews)) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        
        List<ReviewResponseDto> dtos = allReviews.stream()
                                            .map(this::convertToDtoSafe)
                                            .filter(Objects::nonNull)
                                            .collect(Collectors.toList());
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), dtos.size());
        List<ReviewResponseDto> pageContent = (start <= end && !dtos.isEmpty() && start < dtos.size()) ? dtos.subList(start, end) : Collections.emptyList();
        
        return new PageImpl<>(pageContent, pageable, dtos.size());
    }
    
    @Override
    public Page<ReviewResponseDto> getReviewsByUserId(Long userId, Pageable pageable) throws SQLException {
        List<Review> allReviews = reviewDao.selectReviewsByUserId(userId);
        if (CollectionUtils.isEmpty(allReviews)) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        
        List<ReviewResponseDto> dtos = allReviews.stream()
                                            .map(this::convertToDtoSafe)
                                            .filter(Objects::nonNull)
                                            .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), dtos.size());
        List<ReviewResponseDto> pageContent = (start <= end && !dtos.isEmpty() && start < dtos.size()) ? dtos.subList(start, end) : Collections.emptyList();
        
        return new PageImpl<>(pageContent, pageable, dtos.size()); 
    }

    @Override
    public ReviewResponseDto getReviewDetailsById(Long reviewId) throws SQLException, ResourceNotFoundException {
        Review review = reviewDao.selectReviewById(reviewId);
        if (review == null) {
            throw new ResourceNotFoundException("리뷰를 찾을 수 없습니다: " + reviewId);
        }
        return convertToDto(review);
    }
    
    private ReviewResponseDto convertToDto(Review review) {
        if (review == null) return null;
        
        User user = null;
        String userNickname = "알 수 없는 사용자";
        String userProfileImgUrl = null; 

        if (review.getUserId() != null) {
            try {
                 user = userDao.selectUserById(review.getUserId());
                 if (user != null) {
                     userNickname = user.getUsername();
                     userProfileImgUrl = user.getProfileImage();
                 } else {
                     logger.warn("리뷰 작성자 정보를 찾을 수 없습니다. userId: {}", review.getUserId());
                 }
            } catch (Exception e) {
               logger.error("리뷰 DTO 변환 중 사용자 정보 조회 실패 (리뷰 ID: {}, 사용자 ID: {}): {}", review.getReviewId(), review.getUserId(), e.getMessage());
            }
        }

        Accommodation accommodation = null;
        String accomTitle = "알 수 없는 숙소";
        if (review.getAccommodationId() != null) {
            try {
                accommodation = accommodationDao.getAccommodationById(review.getAccommodationId()); 
                if (accommodation != null) {
                    accomTitle = accommodation.getTitle();
                } else {
                     logger.warn("리뷰의 숙소 정보를 찾을 수 없습니다. accommodationId: {}", review.getAccommodationId());
                }
            } catch (Exception e) {
                logger.error("리뷰 DTO 변환 중 숙소 정보 조회 실패 (리뷰 ID: {}, 숙소 ID: {}): {}", review.getReviewId(), review.getAccommodationId(), e.getMessage());
            }
        }
        
        List<ReviewResponseDto.ImageInfo> imageInfos = Collections.emptyList();
        if (!CollectionUtils.isEmpty(review.getImages())) {
            imageInfos = review.getImages().stream()
                .map(img -> ReviewResponseDto.ImageInfo.builder()
                        .reviewImageId(img.getReviewImageId())
                        .imageUrl(img.getImageUrl())
                        .uploadOrder(img.getUploadOrder())
                        .build())
                .collect(Collectors.toList());
        }

        return ReviewResponseDto.builder()
                .reviewId(review.getReviewId())
                .accommodationId(review.getAccommodationId())
                .accommodationTitle(accomTitle)
                .userId(review.getUserId())
                .userNickname(userNickname)
                .userProfileImageUrl(userProfileImgUrl)
                .reservationId(review.getReservationId())
                .rating(review.getRating())
                .title(review.getTitle())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .images(imageInfos)
                .build();
    }

    private ReviewResponseDto convertToDtoSafe(Review review) {
        try {
            return convertToDto(review);
        } catch (Exception e) {
            logger.error("리뷰 DTO 변환 중 예외 발생 (리뷰 ID: {}): {}", review != null ? review.getReviewId() : "null", e.getMessage(), e);
            return null; 
        }
    }
    
    private String extractS3KeyFromUrl(String imageUrl) {
        if (imageUrl == null || bucketName == null || bucketName.trim().isEmpty()) {
            logger.warn("S3 URL에서 키를 추출할 수 없습니다. imageUrl 또는 bucketName이 null이거나 비어있습니다. imageUrl: {}", imageUrl);
            return null;
        }
        try {
            java.net.URL url = new java.net.URL(imageUrl);
            String path = url.getPath();
            if (path.startsWith("/" + bucketName + "/")) {
                return path.substring(("/" + bucketName + "/").length());
            } else if (url.getHost().startsWith(bucketName + ".s3.")) {
                if (path.startsWith("/")) {
                    return path.substring(1);
                }
                return path; 
            } else if (path.startsWith("/")) {
                 logger.warn("S3 URL 형식이 표준적이지 않으나, 경로의 첫 슬래시를 제거하고 키로 사용합니다. URL: {}", imageUrl);
                 return path.substring(1);
            }
            logger.warn("S3 URL에서 키를 추출하지 못했습니다. URL이 예상된 형식이 아닙니다. URL: {}, Path: {}", imageUrl, path);
            return null;
        } catch (java.net.MalformedURLException e) {
            logger.warn("잘못된 형식의 S3 URL입니다: {}. 오류: {}", imageUrl, e.getMessage());
            return null;
        }
    }

    @Override
    public Double getAverageRatingByAccommodationId(Long accommodationId) throws SQLException {
        return reviewDao.selectAverageRatingByAccommodationId(accommodationId); 
    }

    @Override
    public Integer getReviewCountByAccommodationId(Long accommodationId) throws SQLException {
        return reviewDao.selectCountByAccommodationId(accommodationId); 
    }

    @Override
    public Map<String, Object> getReviewSummaryByAccommodationId(Long accommodationId) throws SQLException {
        return reviewDao.selectReviewSummaryByAccommodationId(accommodationId); 
    }

    @Override
    public List<ReviewResponseDto> getRecentReviews(int limit) throws SQLException {
        List<Review> reviews = reviewDao.selectRecentReviews(limit); 
        if (CollectionUtils.isEmpty(reviews)) return Collections.emptyList();
        return reviews.stream().map(this::convertToDtoSafe).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public Map<Integer, Integer> getRatingDistributionByAccommodationId(Long accommodationId) throws SQLException {
        return reviewDao.selectRatingDistributionByAccommodationId(accommodationId); 
    }

    @Override
    public boolean checkReviewEligibility(Long userId, Long accommodationId) throws SQLException {
        return canUserReviewAccommodation(userId, accommodationId);
    }

    @Override
    public ReviewResponseDto getReviewByReservationId(Long reservationId) throws SQLException {
        Review review = reviewDao.selectReviewByReservationId(reservationId); 
        if (review == null) {
            return null; 
        }
        return convertToDto(review);
    }

    @Override
    public List<ReviewResponseDto> getReviewsByHostId(Long hostId, Integer rating) throws SQLException {
        List<Review> reviews = reviewDao.selectReviewsByHostId(hostId); 
        if (CollectionUtils.isEmpty(reviews)) return Collections.emptyList();
        Stream<Review> reviewStream = reviews.stream();
        if (rating != null) {
            reviewStream = reviewStream.filter(r -> r.getRating() != null && r.getRating().equals(rating));
        }
        return reviewStream.map(this::convertToDtoSafe).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public boolean setReviewImageThumbnail(Long reviewId, Long imageId, Long userId) throws SQLException, UnauthorizedException, ResourceNotFoundException {
        Review review = reviewDao.selectReviewById(reviewId);
        if (review == null) {
            throw new ResourceNotFoundException("리뷰를 찾을 수 없습니다: " + reviewId);
        }
        if (!review.getUserId().equals(userId)) {
             throw new UnauthorizedException("리뷰를 찾을 수 없거나 썸네일 설정 권한이 없습니다.");
        }
        ReviewImage image = reviewDao.selectReviewImageById(imageId);
        if (image == null || !image.getReviewId().equals(reviewId)) {
            throw new ResourceNotFoundException("해당 리뷰의 이미지를 찾을 수 없습니다: " + imageId);
        }
        reviewDao.resetThumbnailStatusByReviewId(reviewId);
        return reviewDao.updateReviewImageThumbnailStatus(imageId, true) > 0;
    }

    @Override
    public boolean updateReviewImageOrder(Long reviewId, List<Long> orderedImageIds, Long userId) throws SQLException, UnauthorizedException, ResourceNotFoundException {
        Review review = reviewDao.selectReviewById(reviewId);
        if (review == null) {
            throw new ResourceNotFoundException("리뷰를 찾을 수 없습니다: " + reviewId);
        }
        if (!review.getUserId().equals(userId)) {
            throw new UnauthorizedException("리뷰를 찾을 수 없거나 이미지 순서 변경 권한이 없습니다.");
        }
        
        List<ReviewImage> currentImages = review.getImages(); 
        if (currentImages == null) {
            currentImages = Collections.emptyList();
        }

        if (currentImages.size() != orderedImageIds.size()) {
            throw new IllegalArgumentException("제공된 이미지 ID 목록의 개수(" + orderedImageIds.size() + 
                                               ")가 현재 리뷰의 이미지 개수(" + currentImages.size() + ")와 일치하지 않습니다.");
        }
        for (Long imgId : orderedImageIds) {
            if (currentImages.stream().noneMatch(ci -> ci.getReviewImageId().equals(imgId))) {
                throw new IllegalArgumentException("제공된 이미지 ID 목록에 유효하지 않은 이미지 ID가 포함되어 있습니다: " + imgId);
            }
        }

        int updatedCount = 0;
        for (int i = 0; i < orderedImageIds.size(); i++) {
            updatedCount += reviewDao.updateReviewImageSortOrder(orderedImageIds.get(i), i);
        }
        return updatedCount == orderedImageIds.size(); 
    }
}
