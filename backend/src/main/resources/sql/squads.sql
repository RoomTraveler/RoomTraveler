CREATE TABLE squads (
                        squad_id INT AUTO_INCREMENT PRIMARY KEY,
                        squad_name VARCHAR(100) NOT NULL,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        created_by INT NOT NULL,
                        FOREIGN KEY (created_by) REFERENCES users(user_id)
);

CREATE TABLE squad_members (
                               squad_member_id INT AUTO_INCREMENT PRIMARY KEY,
                               squad_id INT NOT NULL,
                               user_id bigint unsigned NOT NULL,
                               position int not null,
                               FOREIGN KEY (squad_id) REFERENCES squads(squad_id),
                               FOREIGN KEY (user_id) REFERENCES users(user_id),
                               UNIQUE (squad_id, user_id)
);
