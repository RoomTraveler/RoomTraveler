-- 2) 호스트 추가 정보 테이블: hosts
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
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;