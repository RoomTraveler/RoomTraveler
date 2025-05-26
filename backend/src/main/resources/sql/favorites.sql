-- 즐겨찾기(찜) 테이블: favorites
CREATE TABLE favorites (
                           favorite_id      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                           user_id          BIGINT UNSIGNED NOT NULL,
                           accommodation_id BIGINT UNSIGNED NOT NULL,
                           created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           deleted_at       DATETIME DEFAULT NULL,
                           PRIMARY KEY (favorite_id),
                           UNIQUE KEY uq_favorites_user_acc (user_id, accommodation_id)
);