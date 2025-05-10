-- 결제 테이블 생성
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

-- 인덱스 생성
CREATE INDEX idx_payments_reservation_id ON payments(reservation_id);
CREATE INDEX idx_payments_user_id ON payments(user_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_payment_method ON payments(payment_method);
CREATE INDEX idx_payments_created_at ON payments(created_at);

-- 테스트 데이터 삽입
INSERT INTO payments (reservation_id, user_id, payment_method, payment_key, amount, currency, status, card_info, paid_at, created_at, updated_at)
VALUES 
(1, 1, 'CARD', 'pay_test_card_001', 150000.00, 'KRW', 'COMPLETED', '123456******7890', NOW(), NOW(), NOW()),
(2, 2, 'BANK_TRANSFER', 'pay_test_bank_001', 200000.00, 'KRW', 'COMPLETED', NULL, NOW(), NOW(), NOW()),
(3, 1, 'PHONE', 'pay_test_phone_001', 120000.00, 'KRW', 'COMPLETED', NULL, NOW(), NOW(), NOW()),
(4, 3, 'CARD', 'pay_test_card_002', 180000.00, 'KRW', 'CANCELLED', '987654******3210', NOW(), NOW(), NOW());

-- 주석: 이 SQL 파일은 결제 기능을 위한 데이터베이스 스키마를 정의합니다.
