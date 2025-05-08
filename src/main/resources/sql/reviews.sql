-- 리뷰 테이블 생성
CREATE TABLE IF NOT EXISTS reviews (
    review_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    accommodation_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    reservation_id BIGINT UNSIGNED,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    stay_date TIMESTAMP,
    is_verified BOOLEAN DEFAULT FALSE,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (accommodation_id) REFERENCES accommodations(accommodation_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id) ON DELETE SET NULL
);

-- 숙소 샘플 데이터 추가 (테스트용)
-- 리뷰 테스트를 위해 필요한 숙소 데이터가 없는 경우 아래 INSERT 문을 실행하세요
-- INSERT INTO accommodations (accommodation_id, host_id, title, description, address, sido_code, gugun_code, check_in_time, check_out_time, status)
-- VALUES 
-- (1, 4, '서울 시티 호텔', '서울 중심부에 위치한 현대적인 호텔입니다.', '서울특별시 중구 명동길 123', 1, 1, '15:00:00', '11:00:00', 'ACTIVE'),
-- (2, 5, '부산 비치 리조트', '해변가에 위치한 아름다운 리조트입니다.', '부산광역시 해운대구 해운대해변로 456', 2, 2, '16:00:00', '10:00:00', 'ACTIVE'),
-- (3, 4, '제주 오션 뷰 펜션', '제주 바다가 보이는 아늑한 펜션입니다.', '제주특별자치도 서귀포시 중문관광로 789', 3, 3, '14:00:00', '12:00:00', 'ACTIVE');

-- 인덱스 생성
CREATE INDEX idx_reviews_accommodation_id ON reviews(accommodation_id);
CREATE INDEX idx_reviews_user_id ON reviews(user_id);
CREATE INDEX idx_reviews_reservation_id ON reviews(reservation_id);
CREATE INDEX idx_reviews_rating ON reviews(rating);
CREATE INDEX idx_reviews_created_at ON reviews(created_at);

-- 테스트 데이터 삽입
INSERT INTO reviews (accommodation_id, user_id, reservation_id, rating, title, content, stay_date, is_verified, status)
VALUES 
(1, 1, 1, 5, '최고의 숙소였습니다!', '위치도 좋고 시설도 깨끗하고 직원들도 친절했습니다. 다음에도 꼭 방문하고 싶습니다.', '2023-03-15 00:00:00', TRUE, 'ACTIVE'),
(1, 2, 2, 4, '만족스러운 숙박', '전반적으로 만족스러웠습니다. 다만 소음이 조금 있었습니다.', '2023-04-20 00:00:00', TRUE, 'ACTIVE'),
(2, 1, 3, 3, '평범한 숙소', '가격 대비 괜찮았습니다. 특별히 좋거나 나쁘지 않았습니다.', '2023-05-10 00:00:00', TRUE, 'ACTIVE'),
(2, 3, 4, 2, '실망스러웠습니다', '사진과 실제 모습이 많이 달랐고, 청소 상태도 좋지 않았습니다.', '2023-06-05 00:00:00', TRUE, 'ACTIVE'),
(3, 2, 5, 5, '환상적인 경험', '모든 면에서 완벽했습니다. 특히 조식이 정말 맛있었습니다!', '2023-07-12 00:00:00', TRUE, 'ACTIVE');

-- 주석: 이 SQL 파일은 리뷰 기능을 위한 데이터베이스 스키마를 정의합니다.
