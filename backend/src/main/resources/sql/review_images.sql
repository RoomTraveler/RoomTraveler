-- 리뷰 이미지 테이블 생성
CREATE TABLE IF NOT EXISTS review_images (
    image_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '이미지 고유 ID',
    review_id BIGINT UNSIGNED NOT NULL COMMENT '리뷰 ID (reviews 테이블 참조)',
    image_url VARCHAR(1024) NOT NULL COMMENT '이미지 S3 URL',
    caption VARCHAR(512) COMMENT '이미지 캡션',
    is_thumbnail BOOLEAN DEFAULT FALSE COMMENT '썸네일 여부',
    sort_order INT DEFAULT 0 COMMENT '이미지 정렬 순서',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시간',
    FOREIGN KEY (review_id) REFERENCES reviews(review_id) ON DELETE CASCADE
) COMMENT '리뷰에 첨부된 이미지 정보를 저장하는 테이블';

-- 인덱스 생성
CREATE INDEX idx_review_images_review_id ON review_images(review_id);
CREATE INDEX idx_review_images_is_thumbnail ON review_images(is_thumbnail);
CREATE INDEX idx_review_images_sort_order ON review_images(sort_order);

-- 테스트 데이터 삽입 (필요시 reviews.sql에 있는 리뷰 ID를 참조하여 추가)
-- 예시:
-- INSERT INTO review_images (review_id, image_url, caption, is_thumbnail, sort_order)
-- VALUES
-- (1, 'https://example.s3.amazonaws.com/review_images/image1.jpg', '객실 내부 사진', TRUE, 0),
-- (1, 'https://example.s3.amazonaws.com/review_images/image2.jpg', '욕실 사진', FALSE, 1),
-- (2, 'https://example.s3.amazonaws.com/review_images/image3.jpg', '호텔 로비', TRUE, 0);

-- 주석: 이 SQL 파일은 리뷰 이미지 기능을 위한 데이터베이스 스키마를 정의합니다. 