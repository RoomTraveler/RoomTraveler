-- 통합 SQL 파일
-- 이 파일은 모든 SQL 스크립트를 실행 순서에 맞게 통합한 것입니다.

-- -----------------------------------------------------
-- 1. 데이터베이스 기본 구조
-- -----------------------------------------------------

-- 시도 테이블 생성 (지역 정보)
CREATE TABLE IF NOT EXISTS `ssafytrip`.`sidos` (
  `no` INT NOT NULL AUTO_INCREMENT COMMENT '시도번호',
  `sido_code` INT NOT NULL COMMENT '시도코드',
  `sido_name` VARCHAR(20) NULL DEFAULT NULL COMMENT '시도이름',
  PRIMARY KEY (`no`),
  UNIQUE INDEX `sido_code_UNIQUE` (`sido_code` ASC) VISIBLE)
ENGINE = InnoDB
AUTO_INCREMENT = 35
DEFAULT CHARACTER SET = utf8mb4
COMMENT = '시도정보테이블';

-- 구군 테이블 생성 (상세 지역 정보)
CREATE TABLE IF NOT EXISTS `ssafytrip`.`guguns` (
  `no` INT NOT NULL AUTO_INCREMENT COMMENT '구군번호',
  `sido_code` INT NOT NULL COMMENT '시도코드',
  `gugun_code` INT NOT NULL COMMENT '구군코드',
  `gugun_name` VARCHAR(20) NULL DEFAULT NULL COMMENT '구군이름',
  PRIMARY KEY (`no`),
  INDEX `guguns_sido_to_sidos_cdoe_fk_idx` (`sido_code` ASC) VISIBLE,
  INDEX `gugun_code_idx` (`gugun_code` ASC) VISIBLE,
  CONSTRAINT `guguns_sido_to_sidos_cdoe_fk`
    FOREIGN KEY (`sido_code`)
    REFERENCES `ssafytrip`.`sidos` (`sido_code`))
ENGINE = InnoDB
AUTO_INCREMENT = 469
DEFAULT CHARACTER SET = utf8mb4
COMMENT = '구군정보테이블';

-- -----------------------------------------------------
-- 2. 사용자 관련 테이블
-- -----------------------------------------------------

-- 사용자 테이블 생성 (회원 정보)
CREATE TABLE users (
                       user_id         BIGINT UNSIGNED      NOT NULL AUTO_INCREMENT,
                       username        VARCHAR(50)          NOT NULL,    -- 닉네임
                       email           VARCHAR(100)         NOT NULL,    -- 로그인용 이메일
                       password        VARCHAR(255)         NOT NULL,    -- 비밀번호
                       phone           VARCHAR(20)          NULL,        -- 휴대폰 번호
                       role            ENUM('USER','HOST','ADMIN')
                   NOT NULL DEFAULT 'USER',         -- 권한 구분
                       status          ENUM('ACTIVE','INACTIVE','SUSPENDED')
                   NOT NULL DEFAULT 'ACTIVE',       -- 계정 상태
                       created_at      DATETIME             NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at      DATETIME             NOT NULL DEFAULT CURRENT_TIMESTAMP
                           ON UPDATE CURRENT_TIMESTAMP,
                       PRIMARY KEY (user_id),
                       UNIQUE KEY uq_users_email    (email),
                       UNIQUE KEY uq_users_username (username)
);

-- 사용자 샘플 데이터 추가 (테스트용)
-- 비밀번호는 평문으로 저장됩니다.
INSERT INTO users (username, email, password, phone, role, status) VALUES
('beomsoo',   'beomsoo@example.com',    '1234',    '010-1111-2222', 'USER',  'ACTIVE'),
('minji',     'minji.kim@example.com',  '1234',    '010-2222-3333', 'USER',  'ACTIVE'),
('hyunwoo',   'hyunwoo.park@example.com','1234',   '010-3333-4444', 'USER',  'INACTIVE'),
('hostkim',   'host.kim@example.com',   '1234',    '010-4444-5555', 'HOST',  'ACTIVE'),
('hostlee',   'host.lee@example.com',   '1234',    '010-5555-6666', 'HOST',  'ACTIVE'),
('adminlee',  'admin.lee@example.com',  '1234',    '010-6666-7777', 'ADMIN', 'ACTIVE'),
('adminchoi', 'admin.choi@example.com', '1234',    NULL,            'ADMIN', 'SUSPENDED'),
('parkmin',   'min.park@example.com',   '1234',    '010-7777-8888', 'USER',  'ACTIVE'),
('kimyoung',  'young.kim@example.com',  '1234',    '010-8888-9999', 'USER',  'ACTIVE'),
('choiwon',   'won.choi@example.com',   '1234',    '010-9999-0000', 'USER',  'ACTIVE'),
('ssafy',     'ssafy@example.com',      '1234',    NULL,            'USER',  'ACTIVE');

-- 호스트 테이블 생성 (숙소 제공자 정보)
CREATE TABLE hosts (
                       host_id                   BIGINT UNSIGNED NOT NULL,   -- users.user_id 와 1:1 매핑
                       business_name             VARCHAR(100)     NOT NULL,  -- 업체명
                       business_reg_no           VARCHAR(50)      NOT NULL,  -- 사업자 등록번호
                       bank_account              VARCHAR(100)     NOT NULL,  -- 정산 계좌 정보
                       profile_text              TEXT             NULL,      -- 호스트 소개글
                       host_status               ENUM('PENDING','APPROVED','REJECTED')
                            NOT NULL DEFAULT 'PENDING',-- 심사 상태
                       created_at                DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at                DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                           ON UPDATE CURRENT_TIMESTAMP,
                       PRIMARY KEY (host_id),
                       FOREIGN KEY (host_id) REFERENCES users(user_id)
                           ON DELETE CASCADE
);

-- -----------------------------------------------------
-- 3. 숙소 관련 테이블
-- -----------------------------------------------------

-- 숙소 테이블 생성 (숙박 시설 정보)
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

-- 객실 테이블 생성 (숙소 내 개별 객실 정보)
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

-- 이미지 테이블 생성 (숙소 및 객실 이미지)
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

-- 예약 테이블 생성 (객실 예약 정보)
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

-- 객실 가용성 테이블 생성 (날짜별 객실 예약 가능 여부)
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

-- -----------------------------------------------------
-- 4. 리뷰 관련 테이블
-- -----------------------------------------------------

-- 리뷰 테이블 생성 (숙소 이용 후기)
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
) ENGINE=InnoDB;

-- 리뷰 인덱스 생성 (검색 성능 향상)
CREATE INDEX idx_reviews_accommodation_id ON reviews(accommodation_id);
CREATE INDEX idx_reviews_user_id ON reviews(user_id);
CREATE INDEX idx_reviews_reservation_id ON reviews(reservation_id);
CREATE INDEX idx_reviews_rating ON reviews(rating);
CREATE INDEX idx_reviews_created_at ON reviews(created_at);

-- 리뷰 샘플 데이터 추가 (테스트용)
INSERT INTO reviews (accommodation_id, user_id, reservation_id, rating, title, content, stay_date, is_verified, status)
VALUES 
(1, 1, 1, 5, '최고의 숙소였습니다!', '위치도 좋고 시설도 깨끗하고 직원들도 친절했습니다. 다음에도 꼭 방문하고 싶습니다.', '2023-03-15 00:00:00', TRUE, 'ACTIVE'),
(1, 2, 2, 4, '만족스러운 숙박', '전반적으로 만족스러웠습니다. 다만 소음이 조금 있었습니다.', '2023-04-20 00:00:00', TRUE, 'ACTIVE'),
(2, 1, 3, 3, '평범한 숙소', '가격 대비 괜찮았습니다. 특별히 좋거나 나쁘지 않았습니다.', '2023-05-10 00:00:00', TRUE, 'ACTIVE'),
(2, 3, 4, 2, '실망스러웠습니다', '사진과 실제 모습이 많이 달랐고, 청소 상태도 좋지 않았습니다.', '2023-06-05 00:00:00', TRUE, 'ACTIVE'),
(3, 2, 5, 5, '환상적인 경험', '모든 면에서 완벽했습니다. 특히 조식이 정말 맛있었습니다!', '2023-07-12 00:00:00', TRUE, 'ACTIVE');

-- -----------------------------------------------------
-- 5. 장바구니 관련 테이블
-- -----------------------------------------------------

-- 장바구니 테이블 생성 (사용자별 장바구니)
CREATE TABLE carts (
    cart_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (cart_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE (user_id)
);

-- 장바구니 아이템 테이블 생성 (장바구니에 담긴 객실 정보)
CREATE TABLE cart_items (
    cart_item_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    cart_id BIGINT UNSIGNED NOT NULL,
    room_id BIGINT UNSIGNED NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    guest_count INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (cart_item_id),
    FOREIGN KEY (cart_id) REFERENCES carts(cart_id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES rooms(room_id) ON DELETE CASCADE,
    UNIQUE (cart_id, room_id, check_in_date, check_out_date)
);

-- -----------------------------------------------------
-- 6. 즐겨찾기 관련 테이블
-- -----------------------------------------------------

-- 즐겨찾기(찜) 테이블 생성 (사용자가 찜한 숙소 정보)
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

-- 즐겨찾기 샘플 데이터 추가 (테스트용)
INSERT INTO favorites (user_id, accommodation_id) VALUES
(1, 1),  -- 사용자 1이 숙소 1을 찜함
(1, 2),  -- 사용자 1이 숙소 2를 찜함
(2, 1),  -- 사용자 2가 숙소 1을 찜함
(3, 3);  -- 사용자 3이 숙소 3을 찜함

-- -----------------------------------------------------
-- 7. 알림 관련 테이블
-- -----------------------------------------------------

-- 알림 테이블 생성 (사용자에게 전송되는 알림 정보)
CREATE TABLE IF NOT EXISTS notifications (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    reference_id BIGINT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 알림 인덱스 생성 (검색 성능 향상)
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);

-- 알림 샘플 데이터 추가 (테스트용)
INSERT INTO notifications (user_id, title, content, notification_type, reference_id, is_read)
VALUES 
(1, '예약 확정', '귀하의 예약이 확정되었습니다.', 'BOOKING', 101, FALSE),
(1, '리뷰 알림', '귀하의 숙소에 새로운 리뷰가 작성되었습니다.', 'REVIEW', 201, FALSE),
(2, '예약 취소', '귀하의 예약이 취소되었습니다.', 'CANCELLATION', 102, TRUE),
(2, '시스템 알림', '시스템 점검 안내: 오늘 밤 12시부터 2시간 동안 서비스가 중단됩니다.', 'SYSTEM', NULL, FALSE);

-- -----------------------------------------------------
-- 8. 결제 관련 테이블
-- -----------------------------------------------------

-- 결제 테이블 생성 (예약에 대한 결제 정보)
CREATE TABLE IF NOT EXISTS payments (
    payment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    payment_key VARCHAR(255),
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'KRW',
    status VARCHAR(20) NOT NULL,
    card_info VARCHAR(255),
    bank_info VARCHAR(255),
    phone_info VARCHAR(255),
    paid_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    fail_reason VARCHAR(255),
    cancel_reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 결제 인덱스 생성 (검색 성능 향상)
CREATE INDEX idx_payments_reservation_id ON payments(reservation_id);
CREATE INDEX idx_payments_user_id ON payments(user_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_payment_method ON payments(payment_method);
CREATE INDEX idx_payments_created_at ON payments(created_at);

-- 결제 샘플 데이터 추가 (테스트용)
INSERT INTO payments (reservation_id, user_id, payment_method, payment_key, amount, currency, status, card_info, paid_at, created_at, updated_at)
VALUES 
(1, 1, 'CARD', 'pay_test_card_001', 150000.00, 'KRW', 'COMPLETED', '123456******7890', NOW(), NOW(), NOW()),
(2, 2, 'BANK_TRANSFER', 'pay_test_bank_001', 200000.00, 'KRW', 'COMPLETED', NULL, NOW(), NOW(), NOW()),
(3, 1, 'PHONE', 'pay_test_phone_001', 120000.00, 'KRW', 'COMPLETED', NULL, NOW(), NOW(), NOW()),
(4, 3, 'CARD', 'pay_test_card_002', 180000.00, 'KRW', 'CANCELLED', '987654******3210', NOW(), NOW(), NOW());
