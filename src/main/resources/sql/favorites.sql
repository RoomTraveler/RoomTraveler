-- 즐겨찾기(찜) 테이블: favorites
CREATE TABLE favorites (
    favorite_id     BIGINT UNSIGNED      NOT NULL AUTO_INCREMENT,
    user_id         BIGINT UNSIGNED      NOT NULL,
    accommodation_id BIGINT UNSIGNED     NOT NULL,
    created_at      DATETIME             NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (favorite_id),
    UNIQUE KEY uq_favorites_user_accommodation (user_id, accommodation_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (accommodation_id) REFERENCES accommodations(accommodation_id) ON DELETE CASCADE
);

-- 샘플 데이터 (테스트용)
INSERT INTO favorites (user_id, accommodation_id) VALUES
(1, 1),  -- 사용자 1이 숙소 1을 찜함
(1, 2),  -- 사용자 1이 숙소 2를 찜함
(2, 1),  -- 사용자 2가 숙소 1을 찜함
(3, 3);  -- 사용자 3이 숙소 3을 찜함