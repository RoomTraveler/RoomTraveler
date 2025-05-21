package com.ssafy.trip.review.service;

import com.ssafy.trip.review.model.Review;
import org.springframework.web.multipart.MultipartFile;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 리뷰 서비스 인터페이스
 * 리뷰 관련 비즈니스 로직을 처리합니다.
 */
public interface ReviewService {
    /**
     * 새 리뷰를 생성합니다.
     * 사용자 ID를 포함하여 리뷰를 생성하고, 첨부된 이미지 파일들을 S3에 업로드 후 URL을 DB에 저장합니다.
     * 생성된 리뷰 ID를 반환합니다.
     *
     * @param review 생성할 리뷰 정보
     * @param userId 작성자 ID
     * @param imageFiles 첨부된 이미지 파일 목록 (없을 경우 null 또는 빈 리스트)
     * @param captions 이미지에 대한 캡션 목록 (없을 경우 null 또는 빈 리스트)
     * @return 생성된 리뷰 ID
     * @throws SQLException 데이터베이스 오류 발생 시
     * @throws java.io.IOException 파일 처리 오류 발생 시
     */
    Long createReview(Review review, Long userId, List<MultipartFile> imageFiles, List<String> captions) throws SQLException, java.io.IOException;

    /**
     * 리뷰 ID로 리뷰를 조회합니다. 리뷰에 포함된 이미지 정보도 함께 조회됩니다.
     *
     * @param reviewId 조회할 리뷰 ID
     * @return 조회된 리뷰 정보. 해당 ID의 리뷰가 없으면 null 반환.
     * @throws SQLException 데이터베이스 오류 발생 시
     */
    Review getReviewById(Long reviewId) throws SQLException;

    /**
     * 숙소 ID로 리뷰 목록을 조회합니다. 각 리뷰에 포함된 이미지 정보도 함께 조회됩니다.
     *
     * @param accommodationId 조회할 숙소 ID
     * @return 해당 숙소의 리뷰 목록
     * @throws SQLException 데이터베이스 오류 발생 시
     */
    List<Review> getReviewsByAccommodationId(Long accommodationId) throws SQLException;

    /**
     * 사용자 ID로 리뷰 목록을 조회합니다. 각 리뷰에 포함된 이미지 정보도 함께 조회됩니다.
     *
     * @param userId 조회할 사용자 ID
     * @return 해당 사용자가 작성한 리뷰 목록
     * @throws SQLException 데이터베이스 오류 발생 시
     */
    List<Review> getReviewsByUserId(Long userId) throws SQLException;

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
     * 리뷰를 업데이트합니다.
     * 리뷰 작성자만 해당 리뷰를 수정할 수 있습니다.
     * 기존 이미지를 삭제하거나 새 이미지를 추가할 수 있습니다.
     *
     * @param review 업데이트할 리뷰 정보 (텍스트 내용)
     * @param userId 수정 요청 사용자 ID
     * @param newImageFiles 새로 추가할 이미지 파일 목록
     * @param newCaptions 새로 추가할 이미지에 대한 캡션 목록
     * @param deleteImageIds 삭제할 기존 이미지의 ID 목록
     * @return 업데이트 성공 시 true, 실패 시 false
     * @throws SQLException 데이터베이스 오류 발생 시, 또는 권한 없는 사용자의 수정 시도
     * @throws java.io.IOException 파일 처리 오류 발생 시
     */
    boolean updateReview(Review review, Long userId, List<MultipartFile> newImageFiles, List<String> newCaptions, List<Long> deleteImageIds) throws SQLException, java.io.IOException;

    /**
     * 리뷰 상태를 업데이트합니다.
     * 리뷰 작성자 또는 관리자만 상태를 변경할 수 있습니다.
     *
     * @param reviewId 상태를 변경할 리뷰 ID
     * @param status 새로운 상태
     * @param userId 상태 변경 요청 사용자 ID
     * @param userRole 상태 변경 요청 사용자의 역할 (예: "ADMIN")
     * @return 업데이트 성공 시 true, 실패 시 false
     * @throws SQLException 데이터베이스 오류 발생 시, 또는 권한 없는 사용자의 상태 변경 시도
     */
    boolean updateReviewStatus(Long reviewId, String status, Long userId, String userRole) throws SQLException;

    /**
     * 리뷰를 삭제합니다.
     * 리뷰 작성자 또는 관리자만 리뷰를 삭제할 수 있습니다.
     * 리뷰 삭제 시 S3에 업로드된 관련 이미지들도 함께 삭제됩니다.
     *
     * @param reviewId 삭제할 리뷰 ID
     * @param userId 삭제 요청 사용자 ID
     * @param userRole 삭제 요청 사용자의 역할 (예: "ADMIN")
     * @return 삭제 성공 시 true, 실패 시 false
     * @throws SQLException 데이터베이스 오류 발생 시, 또는 권한 없는 사용자의 삭제 시도
     * @throws java.io.IOException S3 이미지 삭제 중 오류 발생 시
     */
    boolean deleteReview(Long reviewId, Long userId, String userRole) throws SQLException, java.io.IOException;

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
    List<Review> getRecentReviews(int limit) throws SQLException;

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
    Review getReviewByReservationId(Long reservationId) throws SQLException;

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

    /**
     * 특정 리뷰 이미지의 캡션을 업데이트합니다.
     *
     * @param reviewId 리뷰 ID
     * @param imageId 캡션을 수정할 이미지 ID
     * @param caption 새로운 캡션 내용
     * @param userId 요청 사용자 ID (리뷰 작성자 또는 관리자 확인용)
     * @return 성공 시 true, 실패 시 false
     * @throws SQLException 데이터베이스 오류 또는 권한 없음
     */
    boolean updateReviewImageCaption(Long reviewId, Long imageId, String caption, Long userId) throws SQLException;
}
