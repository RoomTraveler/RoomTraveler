CREATE TABLE event_board (
                             event_id     BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY, -- 게시글 PK
                             title        VARCHAR(200)     NOT NULL,                           -- 제목
                             content      MEDIUMTEXT       NOT NULL,                           -- 본문(HTML, 이미지 포함)
                             writer       VARCHAR(100)     NOT NULL,                           -- 작성자(관리자 등)
                             start_date   DATE             NOT NULL,                           -- 이벤트 시작일
                             end_date     DATE             NOT NULL,                           -- 이벤트 종료일
                             status       ENUM('ONGOING', 'ENDED', 'HIDDEN') DEFAULT 'ONGOING',-- 상태
                             view_count   INT UNSIGNED     DEFAULT 0,                          -- 조회수
                             created_at   DATETIME         DEFAULT CURRENT_TIMESTAMP,          -- 등록일
                             updated_at   DATETIME         DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP -- 수정일
);

CREATE TABLE event_board_img (
                                 img_id      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                 event_id    BIGINT UNSIGNED NOT NULL,
                                 img_url     VARCHAR(500)    NOT NULL,
                                 is_thumbnail BOOLEAN        DEFAULT FALSE,
                                 created_at  DATETIME        DEFAULT CURRENT_TIMESTAMP,
                                 FOREIGN KEY (event_id) REFERENCES event_board(event_id) ON DELETE CASCADE
);

ALTER TABLE event_board_img ADD COLUMN is_thumbnail BOOLEAN DEFAULT FALSE;
