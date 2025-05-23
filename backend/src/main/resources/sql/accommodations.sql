-- 숙소 테이블
CREATE TABLE accommodations (
                                accommodation_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                host_id BIGINT UNSIGNED NOT NULL,
                                title VARCHAR(100) NOT NULL,
                                description TEXT NOT NULL,
                                address VARCHAR(255) NOT NULL,
                                sido_code INT NOT NULL,
                                gugun_code INT NOT NULL,
                                latitude DECIMAL(20,17) NULL,
                                longitude DECIMAL(20,17) NULL,
                                accommodation_type VARCHAR(50) NULL,
                                phone VARCHAR(20) NULL,
                                email VARCHAR(100) NULL,
                                website VARCHAR(255) NULL,
                                check_in_time TIME NOT NULL,
                                check_out_time TIME NOT NULL,
                                amenities TEXT NULL,
                                status ENUM('ACTIVE', 'INACTIVE', 'PENDING_REVIEW') NOT NULL DEFAULT 'PENDING_REVIEW',
                                avg_review_rating DECIMAL(3,2) NOT NULL DEFAULT 0.00 COMMENT '평균 리뷰 점수 (0~5점, 소수 2자리)',
                                review_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '리뷰 개수',
                                recommend_score DECIMAL(8,4) NOT NULL DEFAULT 0.0000 COMMENT '추천 점수 (평균별점 × log(리뷰수+1))',
                                min_room_price DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '숙소 내 객실 최저가',
                                max_room_price DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '숙소 내 객실 최고가',
                                created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                PRIMARY KEY (accommodation_id),
                                FOREIGN KEY (host_id) REFERENCES users(user_id) ON DELETE CASCADE,
                                FOREIGN KEY (sido_code) REFERENCES sidos(sido_code),
                                FOREIGN KEY (gugun_code) REFERENCES guguns(gugun_code)
);



ALTER TABLE accommodations
    ADD COLUMN avg_review_rating DECIMAL(3,2) NOT NULL DEFAULT 0.00 AFTER status,
    ADD COLUMN review_count INT UNSIGNED NOT NULL DEFAULT 0 AFTER avg_review_rating,
    ADD COLUMN recommend_score DECIMAL(8,4) NOT NULL DEFAULT 0.0000;

ALTER TABLE accommodations
    ADD COLUMN min_room_price DECIMAL(10, 2) DEFAULT 0.00 COMMENT '숙소 내 객실 최저가',
    ADD COLUMN max_room_price DECIMAL(10, 2) DEFAULT 0.00 COMMENT '숙소 내 객실 최고가';


-- 객실 테이블
CREATE TABLE IF NOT EXISTS rooms (
  room_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  accommodation_id BIGINT UNSIGNED NOT NULL,
  name VARCHAR(100)       NOT NULL,
  description TEXT,
  price_per_night DECIMAL(10,2) NOT NULL DEFAULT 0, -- 최종 판매가 (할인 적용되었을 수 있음)
  original_price DECIMAL(10,2) NULL,             -- 원래 가격 (할인 전)
  discount_rate DECIMAL(5,4) NULL,                -- 할인율 (예: 0.1100은 11%)
  cancellation_policy VARCHAR(255) NULL,          -- 취소 및 환불 정책
  capacity INT,
  room_count INT DEFAULT 1 NOT NULL, -- 해당 타입의 객실 총 수 (기본값 1, 추가됨)
  room_type VARCHAR(50),
  bed_type VARCHAR(50),
  bathroom_count INT,
  amenities TEXT,
  status VARCHAR(20), -- 예: AVAILABLE, UNAVAILABLE, MAINTENANCE
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (room_id),
  FOREIGN KEY (accommodation_id)
      REFERENCES accommodations(accommodation_id)
      ON DELETE CASCADE
);

ALTER TABLE rooms ADD COLUMN room_count INT NOT NULL DEFAULT 1 COMMENT '해당 타입 객실의 총 보유량';
UPDATE rooms
SET room_count = 1
WHERE room_count IS NULL OR room_count = 0; -- 이미 설정된 값이 있다면 건드리지 않거나, 특정 조건의 객실만 업데이트

ALTER TABLE rooms
    ADD COLUMN original_price DECIMAL(10,2) NULL COMMENT '원래 가격 (할인 전)',
    ADD COLUMN discount_rate DECIMAL(5,4) NULL COMMENT '할인율 (예: 0.1100은 11%)',
    ADD COLUMN cancellation_policy VARCHAR(255) NULL COMMENT '취소 및 환불 정책';

UPDATE rooms
SET
    original_price = price_per_night, -- 현재 판매가를 원래 가격으로 우선 설정 (정책에 따라 다를 수 있음)
    discount_rate = 0.00,             -- 기본 할인율 0%
    cancellation_policy = '숙소의 기본 취소 정책을 따릅니다. 예약 시 확인해주세요.' -- 기본 문구
WHERE
    original_price IS NULL; -- 아직 설정되지 않은 행에 대해서만 실행 (선택적)

UPDATE rooms SET status = 'ACTIVE' WHERE status = 'AVAILABLE';
-- 이미지 테이블
CREATE TABLE images (
    image_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    reference_id BIGINT UNSIGNED NOT NULL,
    reference_type ENUM('ACCOMMODATION', 'ROOM') NOT NULL,
    accommodation_id BIGINT UNSIGNED NULL,
    room_id BIGINT UNSIGNED NULL,
    image_url VARCHAR(255) NOT NULL,
    caption VARCHAR(255) NULL,
    is_main BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (image_id),
    INDEX (reference_id, reference_type),
    FOREIGN KEY (accommodation_id) REFERENCES accommodations(accommodation_id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES rooms(room_id) ON DELETE CASCADE
);

-- 트리거 대신 외래 키 제약 조건을 사용하여 이미지 삭제 처리

-- 예약 테이블
CREATE TABLE reservations (
    reservation_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    room_id BIGINT UNSIGNED NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    guest_count INT NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED', 'NO_SHOW') NOT NULL DEFAULT 'PENDING',
    payment_status ENUM('UNPAID', 'PAID', 'REFUNDED', 'PARTIALLY_REFUNDED') NOT NULL DEFAULT 'UNPAID',
    special_requests TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (reservation_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES rooms(room_id) ON DELETE CASCADE,
    INDEX (check_in_date, check_out_date)
);

-- 리뷰 테이블은 reviews.sql에 정의되어 있습니다

-- 객실 가용성 테이블
CREATE TABLE room_availability (
    availability_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    room_id BIGINT UNSIGNED NOT NULL,
    date DATE NOT NULL,
    available_count INT NOT NULL, -- 해당 날짜에 예약 가능한 실제 객실 수 (예약에 따라 변동)
    price DECIMAL(10,2) NULL,     -- 해당 날짜의 특별 가격 (NULL이면 rooms.price_per_night 사용)
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (availability_id),
    FOREIGN KEY (room_id) REFERENCES rooms(room_id) ON DELETE CASCADE,
    UNIQUE (room_id, date)
);
