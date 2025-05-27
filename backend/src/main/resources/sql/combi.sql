-- 0. 외래키 제약 해제 (초기화)
SET FOREIGN_KEY_CHECKS=0;

-- 1. 테이블 삭제 (Drop, 역순)
DROP TABLE IF EXISTS review_images;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS squad_members;
DROP TABLE IF EXISTS squads;
DROP TABLE IF EXISTS plan_attraction;
DROP TABLE IF EXISTS plan_likes;
DROP TABLE IF EXISTS attraction_likes;
DROP TABLE IF EXISTS plan;
DROP TABLE IF EXISTS attractions;
DROP TABLE IF EXISTS contenttypes;
DROP TABLE IF EXISTS guguns;
DROP TABLE IF EXISTS sidos;
DROP TABLE IF EXISTS popularity_stats;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS favorites;
DROP TABLE IF EXISTS event_board_img;
DROP TABLE IF EXISTS event_board;
DROP TABLE IF EXISTS cart_items;
DROP TABLE IF EXISTS carts;
DROP TABLE IF EXISTS room_availability;
DROP TABLE IF EXISTS reservations;
DROP TABLE IF EXISTS images;
DROP TABLE IF EXISTS rooms;
DROP TABLE IF EXISTS accommodations;
DROP TABLE IF EXISTS hosts;
DROP TABLE IF EXISTS users;

-- 2. 테이블 생성 (Create)

-- 사용자 테이블: users
CREATE TABLE users (
                       user_id         BIGINT UNSIGNED      NOT NULL AUTO_INCREMENT,
                       username        VARCHAR(50)          NOT NULL,
                       email           VARCHAR(100)         NOT NULL,
                       password        VARCHAR(255)         NOT NULL,
                       phone           VARCHAR(20)          NULL,
                       profile_image   VARCHAR(255)         NULL,
                       refresh         VARCHAR(500)         NULL,
                       role            ENUM('USER','HOST','ADMIN') NOT NULL DEFAULT 'USER',
                       status          ENUM('ACTIVE','INACTIVE','SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
                       created_at      DATETIME             NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at      DATETIME             NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       PRIMARY KEY (user_id),
                       UNIQUE KEY uq_users_email    (email),
                       UNIQUE KEY uq_users_username (username)
);

-- 호스트 테이블: hosts
CREATE TABLE hosts (
                       host_id             BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '호스트 PK',
                       user_id             BIGINT UNSIGNED NOT NULL COMMENT '회원 PK (users.user_id)',
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

-- 시도 테이블: sidos
CREATE TABLE IF NOT EXISTS sidos (
                                     no INT NOT NULL AUTO_INCREMENT COMMENT '시도번호',
                                     sido_code INT NOT NULL COMMENT '시도코드',
                                     sido_name VARCHAR(20) NULL DEFAULT NULL COMMENT '시도이름',
    sido_img_url VARCHAR(500) NULL DEFAULT NULL COMMENT '시도 이미지 URL',
    PRIMARY KEY (no),
    UNIQUE INDEX sido_code_UNIQUE (sido_code)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='시도정보테이블';

-- 구군 테이블: guguns
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

-- 숙소 테이블: accommodations
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
                                status ENUM('ACTIVE', 'INACTIVE', 'PENDING_REVIEW', 'REJECTED') NOT NULL DEFAULT 'PENDING_REVIEW',
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

-- 객실 테이블: rooms
CREATE TABLE rooms (
                       room_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                       accommodation_id BIGINT UNSIGNED NOT NULL,
                       name VARCHAR(100)       NOT NULL,
                       description TEXT,
                       price_per_night DECIMAL(10,2) NOT NULL DEFAULT 0,
                       original_price DECIMAL(10,2) NULL COMMENT '원래 가격 (할인 전)',
                       discount_rate DECIMAL(5,4) NULL COMMENT '할인율 (예: 0.1100은 11%)',
                       cancellation_policy VARCHAR(255) NULL COMMENT '취소 및 환불 정책',
                       capacity INT,
                       room_count INT NOT NULL DEFAULT 1 COMMENT '해당 타입 객실의 총 보유량',
                       room_type VARCHAR(50),
                       bed_type VARCHAR(50),
                       bathroom_count INT,
                       amenities TEXT,
                       status VARCHAR(20),
                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       PRIMARY KEY (room_id),
                       FOREIGN KEY (accommodation_id) REFERENCES accommodations(accommodation_id) ON DELETE CASCADE
);

-- 이미지 테이블: images
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

-- 예약 테이블: reservations
CREATE TABLE reservations (
                              reservation_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                              user_id BIGINT UNSIGNED NOT NULL,
                              room_id BIGINT UNSIGNED NOT NULL,
                              accommodation_id BIGINT UNSIGNED NOT NULL,
                              merchant_uid VARCHAR(255) NULL UNIQUE,
                              check_in_date DATE NOT NULL,
                              check_out_date DATE NOT NULL,
                              guest_count INT NOT NULL,
                              total_price DECIMAL(10,2) NOT NULL,
                              status ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED', 'NO_SHOW') NOT NULL DEFAULT 'PENDING',
                              payment_status ENUM('UNPAID', 'PAID', 'REFUNDED', 'PARTIALLY_REFUNDED') NOT NULL DEFAULT 'UNPAID',
                              created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              PRIMARY KEY (reservation_id),
                              FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                              FOREIGN KEY (room_id) REFERENCES rooms(room_id) ON DELETE CASCADE,
                              INDEX (check_in_date, check_out_date)
);

-- 객실 가용성 테이블: room_availability
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

-- 장바구니 테이블: carts
CREATE TABLE carts (
                       cart_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                       user_id BIGINT UNSIGNED NOT NULL,
                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       PRIMARY KEY (cart_id),
                       FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                       UNIQUE (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='장바구니 테이블';

-- 장바구니 아이템 테이블: cart_items
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

CREATE INDEX idx_cart_items_cart_id ON cart_items(cart_id);
CREATE INDEX idx_cart_items_deleted_at ON cart_items(deleted_at);

-- 이벤트 게시판: event_board
CREATE TABLE event_board (
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
);

-- 이벤트 이미지: event_board_img
CREATE TABLE event_board_img (
                                 img_id      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                 event_id    BIGINT UNSIGNED NOT NULL,
                                 img_url     VARCHAR(500)    NOT NULL,
                                 is_thumbnail BOOLEAN        DEFAULT FALSE,
                                 created_at  DATETIME        DEFAULT CURRENT_TIMESTAMP,
                                 FOREIGN KEY (event_id) REFERENCES event_board(event_id) ON DELETE CASCADE
);

-- 즐겨찾기(찜): favorites
CREATE TABLE favorites (
                           favorite_id      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                           user_id          BIGINT UNSIGNED NOT NULL,
                           accommodation_id BIGINT UNSIGNED NOT NULL,
                           created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           deleted_at       DATETIME DEFAULT NULL,
                           PRIMARY KEY (favorite_id),
                           UNIQUE KEY uq_favorites_user_acc (user_id, accommodation_id)
);

-- 알림 테이블: notifications
CREATE TABLE notifications (
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
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);

-- 결제 테이블: payments
CREATE TABLE payments (
                          payment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          reservation_id BIGINT UNSIGNED NULL,
                          user_id BIGINT UNSIGNED NOT NULL,
                          merchant_uid VARCHAR(255) NOT NULL,
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
CREATE INDEX idx_payments_reservation_id ON payments(reservation_id);
CREATE INDEX idx_payments_user_id ON payments(user_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_payment_method ON payments(payment_method);
CREATE INDEX idx_payments_created_at ON payments(created_at);
CREATE INDEX idx_payments_merchant_uid ON payments(merchant_uid);
CREATE INDEX idx_payments_payment_key ON payments(payment_key);

-- 인기/랭킹/통계 통합 집계 테이블
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
    UNIQUE KEY uq_pop_stats(
                               stats_type, region_type, region_code, accommodation_id, accommodation_type, period_type, period_value
                           ),
    INDEX idx_rank_region_period (
                                     stats_type, region_type, region_code, accommodation_type, period_type, period_value, popularity_score DESC
                                 ),
    INDEX idx_acc_period (
                             stats_type, accommodation_id, period_type, period_value, popularity_score DESC
                         ),
    INDEX idx_rank_topn (
                            stats_type, region_type, region_code, accommodation_type, period_type, period_value, top_n_rank ASC
                        ),
    INDEX idx_type_region_period (
                                     stats_type, accommodation_type, region_type, region_code, period_type, period_value, popularity_score DESC
                                 ),
    INDEX idx_type_period (
                              stats_type, accommodation_type, period_type, period_value, popularity_score DESC
                          )
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='인기/랭킹/통계 통합 집계 테이블(지역, 숙소, 유형별 랭킹/트렌드)';

-- 콘텐츠 타입 테이블
CREATE TABLE IF NOT EXISTS contenttypes (
                                            content_type_id INT NOT NULL COMMENT '콘텐츠타입번호',
                                            content_type_name VARCHAR(45) NULL DEFAULT NULL COMMENT '콘텐츠타입이름',
    PRIMARY KEY (content_type_id)
    ) ENGINE = InnoDB COMMENT = '콘텐츠타입정보테이블';

-- 명소정보 테이블
CREATE TABLE IF NOT EXISTS attractions (
                                           no INT NOT NULL AUTO_INCREMENT COMMENT '명소코드',
                                           content_id INT NULL DEFAULT NULL COMMENT '콘텐츠번호',
                                           title VARCHAR(500) NULL DEFAULT NULL COMMENT '명소이름',
    content_type_id INT NULL DEFAULT NULL COMMENT '콘텐츠타입',
    area_code INT NULL DEFAULT NULL COMMENT '시도코드',
    si_gun_gu_code INT NULL DEFAULT NULL COMMENT '구군코드',
    first_image1 VARCHAR(100) NULL DEFAULT NULL COMMENT '이미지경로1',
    first_image2 VARCHAR(100) NULL DEFAULT NULL COMMENT '이미지경로2',
    map_level INT NULL DEFAULT NULL COMMENT '줌레벨',
    latitude DECIMAL(20,17) NULL DEFAULT NULL COMMENT '위도',
    longitude DECIMAL(20,17) NULL DEFAULT NULL COMMENT '경도',
    tel VARCHAR(20) NULL DEFAULT NULL COMMENT '전화번호',
    addr1 VARCHAR(100) NULL DEFAULT NULL COMMENT '주소1',
    addr2 VARCHAR(100) NULL DEFAULT NULL COMMENT '주소2',
    homepage VARCHAR(1000) NULL DEFAULT NULL COMMENT '홈페이지',
    overview VARCHAR(10000) NULL DEFAULT NULL COMMENT '설명',
    likes BIGINT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (no),
    INDEX attractions_typeid_to_types_typeid_fk_idx (content_type_id),
    INDEX attractions_sido_to_sidos_code_fk_idx (area_code),
    INDEX attractions_sigungu_to_guguns_gugun_fk_idx (si_gun_gu_code),
    CONSTRAINT attractions_area_to_sidos_code_fk FOREIGN KEY (area_code) REFERENCES sidos(sido_code),
    CONSTRAINT attractions_sigungu_to_guguns_gugun_fk FOREIGN KEY (si_gun_gu_code) REFERENCES guguns(gugun_code),
    CONSTRAINT attractions_typeid_to_types_typeid_fk FOREIGN KEY (content_type_id) REFERENCES contenttypes(content_type_id)
    ) ENGINE = InnoDB AUTO_INCREMENT = 107559 COMMENT = '명소정보테이블';

-- 여행 계획(plan) 테이블
CREATE TABLE IF NOT EXISTS plan (
                                    plan_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                    user_id BIGINT UNSIGNED NOT NULL,
                                    is_shared BOOLEAN NOT NULL DEFAULT FALSE,
                                    travel_date DATE,
                                    likes BIGINT UNSIGNED NOT NULL DEFAULT 0,
                                    PRIMARY KEY (plan_id),
    INDEX fk_travel_user1_idx (user_id),
    CONSTRAINT fk_travel_user1 FOREIGN KEY (user_id) REFERENCES users(user_id)
    ) ENGINE = InnoDB;

-- 여행 계획-명소 연결: plan_attraction
CREATE TABLE IF NOT EXISTS plan_attraction (
                                               plan_attraction_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                               attraction_id INT NOT NULL,
                                               plan_id BIGINT UNSIGNED NOT NULL,
                                               order_num INT NOT NULL,
                                               PRIMARY KEY (plan_attraction_id),
    INDEX fk_plan_attraction_attractions1_idx (attraction_id),
    INDEX fk_plan_attraction_plan1_idx (plan_id),
    CONSTRAINT fk_plan_attraction_attractions1 FOREIGN KEY (attraction_id) REFERENCES attractions(no),
    CONSTRAINT fk_plan_attraction_plan1 FOREIGN KEY (plan_id) REFERENCES plan(plan_id)
    ) ENGINE = InnoDB;

-- 관광지 좋아요: attraction_likes
CREATE TABLE IF NOT EXISTS attraction_likes (
                                                id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                user_id BIGINT UNSIGNED NOT NULL,
                                                attraction_id INT NOT NULL,
                                                PRIMARY KEY (id),
    UNIQUE INDEX user_attraction_unique (user_id, attraction_id),
    CONSTRAINT fk_attraction_likes_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_attraction_likes_attraction FOREIGN KEY (attraction_id) REFERENCES attractions(no)
    ) ENGINE = InnoDB;

-- 여행 계획 좋아요: plan_likes
CREATE TABLE IF NOT EXISTS plan_likes (
                                          id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                          user_id BIGINT UNSIGNED NOT NULL,
                                          plan_id BIGINT UNSIGNED NOT NULL,
                                          PRIMARY KEY (id),
    UNIQUE INDEX user_plan_unique (user_id, plan_id),
    CONSTRAINT fk_plan_likes_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_plan_likes_plan FOREIGN KEY (plan_id) REFERENCES plan(plan_id)
    ) ENGINE = InnoDB;

-- 리뷰 테이블: reviews
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

-- Squad 테이블
CREATE TABLE squads (
                        squad_id INT AUTO_INCREMENT PRIMARY KEY,
                        squad_name VARCHAR(100) NOT NULL,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        created_by BIGINT UNSIGNED NOT NULL,  -- 수정: BIGINT UNSIGNED
                        FOREIGN KEY (created_by) REFERENCES users(user_id)
);


CREATE TABLE squad_members (
                               squad_member_id INT AUTO_INCREMENT PRIMARY KEY,
                               squad_id INT NOT NULL,
                               user_id BIGINT UNSIGNED NOT NULL,
                               position INT NOT NULL,
                               FOREIGN KEY (squad_id) REFERENCES squads(squad_id),
                               FOREIGN KEY (user_id) REFERENCES users(user_id),
                               UNIQUE (squad_id, user_id)
);


-- 3. 외래키 제약 재활성화
SET FOREIGN_KEY_CHECKS=1;
