CREATE TABLE IF NOT EXISTS reviews (
    review_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '리뷰 ID',
    accommodation_id BIGINT NOT NULL COMMENT '숙소 ID',
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    reservation_id BIGINT UNIQUE COMMENT '이 리뷰가 작성된 예약 ID, UNIQUE 제약으로 예약 건당 하나의 리뷰만 허용',
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
    FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id) ON DELETE SET NULL -- 예약 삭제 시 리뷰는 남되 연결만 해제
    -- 또는 ON DELETE CASCADE (예약 삭제 시 관련 리뷰도 함께 삭제) 정책에 따라 결정
) COMMENT '숙소 리뷰 테이블';
