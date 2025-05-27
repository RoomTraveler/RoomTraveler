-- reviews 테이블을 다음과 같이 수정
CREATE TABLE IF NOT EXISTS reviews (
                                       review_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '리뷰 ID',
                                       accommodation_id BIGINT UNSIGNED NOT NULL COMMENT '숙소 ID',           -- UNSIGNED 추가!
                                       user_id BIGINT UNSIGNED NOT NULL COMMENT '사용자 ID',                 -- UNSIGNED 추가!
                                       reservation_id BIGINT UNSIGNED UNIQUE COMMENT '예약 ID',              -- UNSIGNED 추가!
                                       rating INT NOT NULL COMMENT '평점 (1-5)',
                                       title VARCHAR(255) NULL COMMENT '리뷰 제목',
    content TEXT NOT NULL COMMENT '리뷰 내용',
    stay_date DATE NULL COMMENT '숙박 날짜 (선택적)',
    is_verified BOOLEAN DEFAULT FALSE COMMENT '실제 숙박 여부 (관리자가 변경 가능)',
    status VARCHAR(50) DEFAULT 'ACTIVE' COMMENT '리뷰 상태 (ACTIVE, REPORTED, REMOVED 등)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시간',
    FOREIGN KEY (accommodation_id) REFERENCES accommodations(accommodation_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id) ON DELETE SET NULL
    ) COMMENT '숙소 리뷰 테이블';

-- 리뷰 이미지 테이블
CREATE TABLE IF NOT EXISTS review_images (
                                             review_image_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '리뷰 이미지 고유 ID',
                                             review_id BIGINT NOT NULL COMMENT '리뷰 ID (reviews 테이블 FK)',
                                             image_url VARCHAR(2048) NOT NULL COMMENT '이미지 URL (S3 경로 등)',
    upload_order INT DEFAULT 0 COMMENT '이미지 정렬 순서 (0부터 시작)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시간',
    FOREIGN KEY (review_id) REFERENCES reviews(review_id) ON DELETE CASCADE
    ) COMMENT '리뷰 이미지 정보 테이블';



