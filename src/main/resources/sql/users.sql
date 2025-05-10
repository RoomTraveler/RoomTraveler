-- 1) 사용자 테이블: users
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
