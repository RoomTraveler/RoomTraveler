
-- 주석: 이 SQL 파일은 리뷰 이미지 기능을 위한 데이터베이스 스키마를 정의합니다. 
CREATE TABLE IF NOT EXISTS review_images (
    review_image_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '리뷰 이미지 고유 ID',
    review_id BIGINT NOT NULL COMMENT '리뷰 ID (reviews 테이블 FK)',
    image_url VARCHAR(2048) NOT NULL COMMENT '이미지 URL (S3 경로 등)',
    upload_order INT DEFAULT 0 COMMENT '이미지 정렬 순서 (0부터 시작)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시간',
    FOREIGN KEY (review_id) REFERENCES reviews(review_id) ON DELETE CASCADE -- 리뷰 삭제 시 관련 이미지도 함께 삭제
) COMMENT '리뷰 이미지 정보 테이블';