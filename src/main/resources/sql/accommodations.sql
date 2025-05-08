-- Accommodations Table
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
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (accommodation_id),
    FOREIGN KEY (host_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (sido_code) REFERENCES sidos(sido_code),
    FOREIGN KEY (gugun_code) REFERENCES guguns(gugun_code)
);

-- 숙소 샘플 데이터 추가 (테스트용)
INSERT INTO accommodations (accommodation_id, host_id, title, description, address, sido_code, gugun_code, check_in_time, check_out_time, status)
VALUES 
(1, 4, '서울 시티 호텔', '서울 중심부에 위치한 현대적인 호텔입니다.', '서울특별시 중구 명동길 123', 1, 1, '15:00:00', '11:00:00', 'ACTIVE'),
(2, 5, '부산 비치 리조트', '해변가에 위치한 아름다운 리조트입니다.', '부산광역시 해운대구 해운대해변로 456', 2, 2, '16:00:00', '10:00:00', 'ACTIVE'),
(3, 4, '제주 오션 뷰 펜션', '제주 바다가 보이는 아늑한 펜션입니다.', '제주특별자치도 서귀포시 중문관광로 789', 3, 3, '14:00:00', '12:00:00', 'ACTIVE');

-- Rooms Table
-- rooms 테이블 생성
CREATE TABLE IF NOT EXISTS rooms (
  room_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  accommodation_id BIGINT UNSIGNED NOT NULL,
  name VARCHAR(100)       NOT NULL,
  description TEXT,
  capacity INT,
  price_per_night DECIMAL(10,2) NOT NULL DEFAULT 0,
  room_type VARCHAR(50),
                                     bed_type VARCHAR(50),
                                     bathroom_count INT,
                                     amenities TEXT,
                                     status VARCHAR(20),
                                     created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                     PRIMARY KEY (room_id),
                                     FOREIGN KEY (accommodation_id)
                                         REFERENCES accommodations(accommodation_id)
                                         ON DELETE CASCADE
);


-- Images Table
CREATE TABLE images (
    image_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    reference_id BIGINT UNSIGNED NOT NULL,
    reference_type ENUM('ACCOMMODATION', 'ROOM') NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    caption VARCHAR(255) NULL,
    is_main BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (image_id),
    INDEX (reference_id, reference_type)
);

-- Reservations Table
CREATE TABLE reservations (
    reservation_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    room_id BIGINT UNSIGNED NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    guest_count INT NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED') NOT NULL DEFAULT 'PENDING',
    payment_status ENUM('UNPAID', 'PAID', 'REFUNDED') NOT NULL DEFAULT 'UNPAID',
    special_requests TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (reservation_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES rooms(room_id) ON DELETE CASCADE,
    INDEX (check_in_date, check_out_date)
);

-- Reviews Table is now defined in reviews.sql

-- Room Availability Table
CREATE TABLE room_availability (
    availability_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    room_id BIGINT UNSIGNED NOT NULL,
    date DATE NOT NULL,
    available_count INT NOT NULL,
    price DECIMAL(10,2) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (availability_id),
    FOREIGN KEY (room_id) REFERENCES rooms(room_id) ON DELETE CASCADE,
    UNIQUE (room_id, date)
);
