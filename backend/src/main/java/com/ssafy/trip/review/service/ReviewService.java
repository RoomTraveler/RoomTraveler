package com.ssafy.trip.review.service;

import com.ssafy.trip.review.dto.ReviewCreationRequestDto;
import com.ssafy.trip.review.dto.ReviewResponseDto;
import com.ssafy.trip.review.dto.ReviewUpdateRequestDto;
import com.ssafy.trip.exception.ResourceNotFoundException;
import com.ssafy.trip.exception.UnauthorizedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 리뷰 서비스 인터페이스
 * 리뷰 관련 비즈니스 로직을 처리합니다.
 */
public interface ReviewService {
    /**
     * 사용자가 특정 숙소에 대해 리뷰를 작성할 수 있는지 확인합니다.
     * (결제 완료된 예약 건이 있는지 확인)
     */
    boolean canUserReviewAccommodation(Long userId, Long accommodationId) throws SQLException;

    /**
     * 특정 숙소에 대한 리뷰 목록을 페이징하여 조회합니다.
     */
    Page<ReviewResponseDto> getReviewsByAccommodationId(Long accommodationId, Pageable pageable) throws SQLException;

    /**
     * 특정 사용자가 작성한 리뷰 목록을 페이징하여 조회합니다.
     */
    Page<ReviewResponseDto> getReviewsByUserId(Long userId, Pageable pageable) throws SQLException;
    
    /**
     * 리뷰 ID로 상세 정보를 조회합니다.
     */
    ReviewResponseDto getReviewDetailsById(Long reviewId) throws SQLException, ResourceNotFoundException;

    /**
     * 새 리뷰를 생성합니다. 이미지 파일들을 S3에 업로드하고 URL을 DB에 저장합니다.
     */
    ReviewResponseDto createReview(ReviewCreationRequestDto requestDto, List<MultipartFile> imageFiles, Long userId)
            throws SQLException, IOException, UnauthorizedException, ResourceNotFoundException;

    /**
     * 리뷰를 수정합니다. 작성자만 수정 가능합니다.
     * 기존 이미지를 삭제하거나 새 이미지를 추가할 수 있습니다.
     */
    ReviewResponseDto updateReview(Long reviewId, ReviewUpdateRequestDto requestDto, List<MultipartFile> newImageFiles, Long userId)
            throws SQLException, IOException, ResourceNotFoundException, UnauthorizedException;

    /**
     * 리뷰를 삭제합니다. 작성자 또는 관리자만 삭제 가능합니다.
     * S3 이미지도 함께 삭제합니다.
     */
    void deleteReview(Long reviewId, Long userId) throws SQLException, IOException, ResourceNotFoundException, UnauthorizedException;

    /**
     * 숙소 ID로 리뷰 평균 평점을 조회합니다.
     *
     * @param accommodationId 조회할 숙소 ID
     * @return 해당 숙소의 평균 평점. 리뷰가 없으면 0.0 반환.
     * @throws SQLException 데이터베이스 오류 발생 시
     */
    Double getAverageRatingByAccommodationId(Long accommodationId) throws SQLException;

    /**
     * 숙소 ID로 리뷰 개수를 조회합니다.
     *
     * @param accommodationId 조회할 숙소 ID
     * @return 해당 숙소의 리뷰 개수
     * @throws SQLException 데이터베이스 오류 발생 시
     */
    Integer getReviewCountByAccommodationId(Long accommodationId) throws SQLException;

    /**
     * 숙소 ID로 리뷰 요약 정보를 조회합니다.
     * (예: 평균 평점, 리뷰 개수 등)
     *
     * @param accommodationId 조회할 숙소 ID
     * @return 리뷰 요약 정보
     * @throws SQLException 데이터베이스 오류 발생 시
     */
    Map<String, Object> getReviewSummaryByAccommodationId(Long accommodationId) throws SQLException;

    /**
     * 최근 리뷰 목록을 조회합니다. 각 리뷰에 포함된 이미지 정보도 함께 조회됩니다.
     *
     * @param limit 조회할 최근 리뷰의 개수
     * @return 최근 리뷰 목록
     * @throws SQLException 데이터베이스 오류 발생 시
     */
    List<ReviewResponseDto> getRecentReviews(int limit) throws SQLException;

    /**
     * 평점별 리뷰 개수를 조회합니다.
     *
     * @param accommodationId 조회할 숙소 ID
     * @return 평점(키)과 해당 평점의 리뷰 개수(값)를 담은 Map
     * @throws SQLException 데이터베이스 오류 발생 시
     */
    Map<Integer, Integer> getRatingDistributionByAccommodationId(Long accommodationId) throws SQLException;

    /**
     * 리뷰 작성 자격을 확인합니다.
     * 사용자가 해당 숙소에 실제로 숙박했는지 (또는 예약 기록이 있는지 등) 확인합니다.
     *
     * @param userId 확인할 사용자 ID
     * @param accommodationId 확인할 숙소 ID
     * @return 리뷰 작성 자격이 있으면 true, 없으면 false
     * @throws SQLException 데이터베이스 오류 발생 시
     */
    boolean checkReviewEligibility(Long userId, Long accommodationId) throws SQLException;

    /**
     * 예약 ID로 리뷰를 조회합니다. 리뷰에 포함된 이미지 정보도 함께 조회됩니다.
     *
     * @param reservationId 조회할 예약 ID
     * @return 해당 예약에 대한 리뷰 정보. 리뷰가 없으면 null 반환.
     * @throws SQLException 데이터베이스 오류 발생 시
     */
    ReviewResponseDto getReviewByReservationId(Long reservationId) throws SQLException;

    /**
     * 호스트 ID로 해당 호스트가 관리하는 모든 숙소의 리뷰 목록을 조회합니다.
     * 각 리뷰에는 이미지 정보가 포함될 수 있습니다.
     * 필요한 경우 rating으로 필터링합니다.
     *
     * @param hostId 호스트 ID
     * @param rating 필터링할 별점 (선택 사항, null이면 모든 별점)
     * @return 리뷰 목록
     * @throws SQLException 데이터베이스 오류 발생 시
     */
    List<ReviewResponseDto> getReviewsByHostId(Long hostId, Integer rating) throws SQLException;

    /**
     * 특정 리뷰 이미지의 썸네일 상태를 설정합니다.
     * 해당 리뷰의 다른 이미지들은 썸네일 상태가 해제됩니다.
     *
     * @param reviewId 리뷰 ID
     * @param imageId 썸네일로 지정할 이미지 ID
     * @param userId 요청 사용자 ID (리뷰 작성자 또는 관리자 확인용)
     * @return 성공 시 true, 실패 시 false
     * @throws SQLException 데이터베이스 오류 또는 권한 없음
     */
    boolean setReviewImageThumbnail(Long reviewId, Long imageId, Long userId) throws SQLException;

    /**
     * 특정 리뷰에 속한 이미지들의 표시 순서를 업데이트합니다.
     *
     * @param reviewId 리뷰 ID
     * @param orderedImageIds 정렬된 순서대로 이미지 ID 목록
     * @param userId 요청 사용자 ID (리뷰 작성자 또는 관리자 확인용)
     * @return 성공 시 true, 실패 시 false
     * @throws SQLException 데이터베이스 오류 또는 권한 없음
     * @throws IllegalArgumentException 이미지 ID 목록이 유효하지 않을 경우
     */
    boolean updateReviewImageOrder(Long reviewId, List<Long> orderedImageIds, Long userId) throws SQLException;

}
