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


-- -----------------------------------------------------
-- 7. 알림 관련 테이블
-- -----------------------------------------------------

-- 알림 테이블 생성 (사용자에게 전송되는 알림 정보)
CREATE TABLE IF NOT EXISTS notifications (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
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


-- -----------------------------------------------------
-- 8. 결제 관련 테이블
-- -----------------------------------------------------

-- 결제 테이블 생성 (예약에 대한 결제 정보)
CREATE TABLE IF NOT EXISTS payments (
    payment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
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
