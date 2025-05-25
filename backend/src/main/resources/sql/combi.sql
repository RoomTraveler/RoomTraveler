-- ==삭제 코드
DROP TABLE IF EXISTS room_availability;
DROP TABLE IF EXISTS review_images;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS popularity_stats;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS favorites;
DROP TABLE IF EXISTS event_board_img;
DROP TABLE IF EXISTS event_board;
DROP TABLE IF EXISTS cart_items;
DROP TABLE IF EXISTS carts;
DROP TABLE IF EXISTS reservations;
DROP TABLE IF EXISTS images;
DROP TABLE IF EXISTS rooms;
DROP TABLE IF EXISTS accommodations;
DROP TABLE IF EXISTS hosts;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS guguns;
DROP TABLE IF EXISTS sidos;

DROP TABLE IF EXISTS record_image;
DROP TABLE IF EXISTS record;
DROP TABLE IF EXISTS attraction_likes;
DROP TABLE IF EXISTS plan_likes;
DROP TABLE IF EXISTS plan_attraction;
DROP TABLE IF EXISTS plan;
DROP TABLE IF EXISTS attractions;
DROP TABLE IF EXISTS contenttypes;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS guguns;
DROP TABLE IF EXISTS sidos;


-- ================================================
-- 1. 지역(시도/구군) 테이블 (부모테이블, 가장 먼저!)
-- ================================================
CREATE TABLE IF NOT EXISTS sidos (
                                     no INT NOT NULL AUTO_INCREMENT COMMENT '시도번호',
                                     sido_code INT NOT NULL COMMENT '시도코드',
                                     sido_name VARCHAR(20) NULL DEFAULT NULL COMMENT '시도이름',
    sido_img_url VARCHAR(500) NULL DEFAULT NULL COMMENT '시도 이미지 URL',
    PRIMARY KEY (no),
    UNIQUE INDEX sido_code_UNIQUE (sido_code)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='시도정보테이블';

CREATE TABLE IF NOT EXISTS guguns (
                                      no INT NOT NULL AUTO_INCREMENT COMMENT '구군번호',
                                      sido_code INT NOT NULL COMMENT '시도코드',
                                      gugun_code INT NOT NULL COMMENT '구군코드',
                                      gugun_name VARCHAR(20) NULL DEFAULT NULL COMMENT '구군이름',
    PRIMARY KEY (no),
    INDEX guguns_sido_to_sidos_cdoe_fk_idx (sido_code),
    INDEX gugun_code_idx (gugun_code),
    CONSTRAINT guguns_sido_to_sidos_cdoe_fk FOREIGN KEY (sido_code) REFERENCES sidos(sido_code)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='구군정보테이블';

-- ================================================
-- 2. 사용자(users), 호스트(hosts) 테이블
-- ================================================
CREATE TABLE IF NOT EXISTS users (
                                     user_id         BIGINT UNSIGNED      NOT NULL AUTO_INCREMENT,
                                     username        VARCHAR(50)          NOT NULL,    -- 닉네임
    email           VARCHAR(100)         NOT NULL,    -- 로그인용 이메일
    password        VARCHAR(255)         NOT NULL,    -- 비밀번호
    phone           VARCHAR(20)          NULL,        -- 휴대폰 번호
    profile_image   VARCHAR(255)         NULL,        -- 대표 이미지(프로필)
    refresh         VARCHAR(500)         NULL,        -- 리프레시 토큰 등
    role            ENUM('USER','HOST','ADMIN') NOT NULL DEFAULT 'USER', -- 권한 구분
    status          ENUM('ACTIVE','INACTIVE','SUSPENDED') NOT NULL DEFAULT 'ACTIVE', -- 계정 상태
    created_at      DATETIME             NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME             NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    UNIQUE KEY uq_users_email    (email),
    UNIQUE KEY uq_users_username (username)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='사용자 테이블';

CREATE TABLE hosts (
                       host_id             BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '호스트 PK',
                       user_id             BIGINT UNSIGNED NOT NULL COMMENT '회원 PK (users.user_id)', -- ← 여기!
                       business_number     VARCHAR(20) NOT NULL UNIQUE COMMENT '사업자 등록번호',
                       business_name       VARCHAR(100) NOT NULL COMMENT '상호명',
                       ceo_name            VARCHAR(50) NOT NULL COMMENT '대표자명',
                       business_address    VARCHAR(255) NOT NULL COMMENT '사업장 주소',
                       business_phone      VARCHAR(20) COMMENT '사업장 전화번호',
                       business_license    VARCHAR(255) NOT NULL COMMENT '사업자 등록증 이미지 URL',
                       business_license_expire DATE COMMENT '사업자 등록증 만료일',
                       license_resubmit_url VARCHAR(255) COMMENT '사업자 등록증 재제출 이미지 URL',
                       license_resubmitted_at DATETIME COMMENT '사업자 등록증 재제출 일시',
                       bank_name           VARCHAR(30) NOT NULL COMMENT '정산 은행명',
                       bank_account        VARCHAR(30) NOT NULL COMMENT '정산 계좌번호',
                       bank_owner          VARCHAR(50) NOT NULL COMMENT '정산 예금주명',
                       description         TEXT COMMENT '호스트 자기소개',
                       latitude            DECIMAL(10,7) COMMENT '위도',
                       longitude           DECIMAL(10,7) COMMENT '경도',
                       status              VARCHAR(20) DEFAULT 'WAIT' COMMENT '상태(WAIT: 심사중, ACTIVE: 승인, REJECT: 반려)',
                       admin_comment       VARCHAR(255) COMMENT '관리자 심사 코멘트',
                       approved_at         DATETIME COMMENT '최초 승인일',
                       rejected_at         DATETIME COMMENT '최초 반려일',
                       business_type       VARCHAR(50) COMMENT '사업자 업종(호텔, 펜션 등)',
                       created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
                       updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ================================================
-- 3. 숙소(accommodations) 테이블
-- ================================================
CREATE TABLE IF NOT EXISTS accommodations (
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
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='숙소 테이블';

-- ================================================
-- 4. 객실(rooms) 테이블
-- ================================================
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
    room_count INT DEFAULT 1 NOT NULL, -- 해당 타입의 객실 총 수 (기본값 1)
    room_type VARCHAR(50),
    bed_type VARCHAR(50),
    bathroom_count INT,
    amenities TEXT,
    status VARCHAR(20), -- 예: ACTIVE, UNAVAILABLE, MAINTENANCE
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (room_id),
    FOREIGN KEY (accommodation_id) REFERENCES accommodations(accommodation_id) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='객실 테이블';

-- ================================================
-- 5. 숙소/객실 이미지(images) 테이블
-- ================================================
CREATE TABLE IF NOT EXISTS images (
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
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='숙소/객실 이미지 테이블';

-- ================================================
-- 6. 예약(reservations) 테이블
-- ================================================
CREATE TABLE IF NOT EXISTS reservations (
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
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='예약 테이블';

-- ================================================
-- 7. 장바구니(carts, cart_items) 테이블
-- ================================================
-- 장바구니 테이블
CREATE TABLE carts (
                       cart_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                       user_id BIGINT UNSIGNED NOT NULL,
                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       PRIMARY KEY (cart_id),
                       FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                       UNIQUE (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='장바구니 테이블';

-- 장바구니 아이템 테이블 (soft delete만 포함, 특이 요청 없음)
CREATE TABLE cart_items (
                            cart_item_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                            cart_id BIGINT UNSIGNED NOT NULL,
                            room_id BIGINT UNSIGNED NOT NULL,
                            check_in_date DATE NOT NULL,
                            check_out_date DATE NOT NULL,
                            guest_count INT NOT NULL,
                            price DECIMAL(10,2) NOT NULL,
                            deleted_at DATETIME DEFAULT NULL COMMENT '삭제시각(soft delete)',
                            created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            PRIMARY KEY (cart_item_id),
                            FOREIGN KEY (cart_id) REFERENCES carts(cart_id) ON DELETE CASCADE,
                            FOREIGN KEY (room_id) REFERENCES rooms(room_id) ON DELETE CASCADE,
                            UNIQUE (cart_id, room_id, check_in_date, check_out_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='장바구니 아이템 테이블';

-- 인덱스 권장 (옵션)
CREATE INDEX idx_cart_items_cart_id ON cart_items(cart_id);
CREATE INDEX idx_cart_items_deleted_at ON cart_items(deleted_at);
-- ================================================
-- 8. 이벤트(event_board, event_board_img) 테이블
-- ================================================
CREATE TABLE IF NOT EXISTS event_board (
                                           event_id     BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                           title        VARCHAR(200)     NOT NULL,
    content      MEDIUMTEXT       NOT NULL,
    writer       VARCHAR(100)     NOT NULL,
    start_date   DATE             NOT NULL,
    end_date     DATE             NOT NULL,
    status       ENUM('ONGOING', 'ENDED', 'HIDDEN') DEFAULT 'ONGOING',
    view_count   INT UNSIGNED     DEFAULT 0,
    created_at   DATETIME         DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME         DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='이벤트 게시판';

CREATE TABLE IF NOT EXISTS event_board_img (
                                               img_id      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                               event_id    BIGINT UNSIGNED NOT NULL,
                                               img_url     VARCHAR(500)    NOT NULL,
    is_thumbnail BOOLEAN        DEFAULT FALSE,
    created_at  DATETIME        DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES event_board(event_id) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='이벤트 게시판 이미지';

-- ================================================
-- 9. 즐겨찾기(찜) 테이블
-- ================================================
CREATE TABLE favorites (
                           favorite_id      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                           user_id          BIGINT UNSIGNED NOT NULL,
                           accommodation_id BIGINT UNSIGNED NOT NULL,
                           created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           deleted_at       DATETIME DEFAULT NULL,
                           PRIMARY KEY (favorite_id),
                           UNIQUE KEY uq_favorites_user_acc (user_id, accommodation_id)
);

-- ================================================
-- 10. 알림(notifications) 테이블
-- ================================================
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
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='알림 테이블';

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);

-- ================================================
-- 11. 결제(payments) 테이블
-- ================================================
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
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='결제 테이블';

CREATE INDEX idx_payments_reservation_id ON payments(reservation_id);
CREATE INDEX idx_payments_user_id ON payments(user_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_payment_method ON payments(payment_method);
CREATE INDEX idx_payments_created_at ON payments(created_at);

-- ================================================
-- 12. 인기/랭킹/통계(popularity_stats) 테이블
-- ================================================
CREATE TABLE IF NOT EXISTS popularity_stats (
                                                id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '집계 PK',
                                                stats_type VARCHAR(30) NOT NULL COMMENT '집계 유형: REGION, ACCOMMODATION, TYPE, TYPE_REGION',
    region_type VARCHAR(10) NULL COMMENT '집계 단위: sido(시도), gugun(구군), NULL',
    region_code INT NULL COMMENT '지역 코드: sido_code/gugun_code, NULL',
    accommodation_id BIGINT NULL COMMENT '숙소 PK (ACCOMMODATION 타입일 때만)',
    accommodation_type VARCHAR(50) NULL COMMENT '숙소 유형 (호텔/펜션 등, TYPE, TYPE_REGION일 때)',
    period_type VARCHAR(10) NOT NULL COMMENT '집계 단위: DAILY/WEEKLY/MONTHLY',
    period_value DATE NOT NULL COMMENT '집계 기준일자',
    reservation_count INT DEFAULT 0 COMMENT '예약수',
    view_count INT DEFAULT 0 COMMENT '조회수',
    review_count INT DEFAULT 0 COMMENT '리뷰수',
    popularity_score DOUBLE DEFAULT 0 COMMENT '종합 인기점수(예약*2 + 조회*1 등 가중치)',
    top_n_rank INT DEFAULT NULL COMMENT '랭킹(동일 단위 내 순위)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '집계 생성일',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '집계 수정일',
    UNIQUE KEY uq_pop_stats(stats_type, region_type, region_code, accommodation_id, accommodation_type, period_type, period_value),
    INDEX idx_rank_region_period (stats_type, region_type, region_code, accommodation_type, period_type, period_value, popularity_score DESC),
    INDEX idx_acc_period (stats_type, accommodation_id, period_type, period_value, popularity_score DESC),
    INDEX idx_rank_topn (stats_type, region_type, region_code, accommodation_type, period_type, period_value, top_n_rank ASC),
    INDEX idx_type_region_period (stats_type, accommodation_type, region_type, region_code, period_type, period_value, popularity_score DESC),
    INDEX idx_type_period (stats_type, accommodation_type, period_type, period_value, popularity_score DESC)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='인기/랭킹/통계 통합 집계 테이블';

-- ================================================
-- 13. 리뷰(reviews) 테이블
-- ================================================
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
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='리뷰 테이블';

CREATE INDEX idx_reviews_accommodation_id ON reviews(accommodation_id);
CREATE INDEX idx_reviews_user_id ON reviews(user_id);
CREATE INDEX idx_reviews_reservation_id ON reviews(reservation_id);
CREATE INDEX idx_reviews_rating ON reviews(rating);
CREATE INDEX idx_reviews_created_at ON reviews(created_at);

-- ================================================
-- 14. 리뷰 이미지(review_images) 테이블
-- ================================================
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
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='리뷰에 첨부된 이미지 정보';

CREATE INDEX idx_review_images_review_id ON review_images(review_id);
CREATE INDEX idx_review_images_is_thumbnail ON review_images(is_thumbnail);
CREATE INDEX idx_review_images_sort_order ON review_images(sort_order);

-- ================================================
-- 15. 객실 가용성(room_availability) 테이블
-- ================================================
CREATE TABLE IF NOT EXISTS room_availability (
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
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='객실 가용성 테이블';