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