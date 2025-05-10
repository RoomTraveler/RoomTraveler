-- 알림 테이블 생성
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

-- 인덱스 생성
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);

-- 테스트 데이터 삽입
INSERT INTO notifications (user_id, title, content, notification_type, reference_id, is_read)
VALUES 
(1, '예약 확정', '귀하의 예약이 확정되었습니다.', 'BOOKING', 101, FALSE),
(1, '리뷰 알림', '귀하의 숙소에 새로운 리뷰가 작성되었습니다.', 'REVIEW', 201, FALSE),
(2, '예약 취소', '귀하의 예약이 취소되었습니다.', 'CANCELLATION', 102, TRUE),
(2, '시스템 알림', '시스템 점검 안내: 오늘 밤 12시부터 2시간 동안 서비스가 중단됩니다.', 'SYSTEM', NULL, FALSE);

-- 주석: 이 SQL 파일은 알림 기능을 위한 데이터베이스 스키마를 정의합니다.
