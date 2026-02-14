CREATE TABLE roulette_histories (
    id          BIGINT    AUTO_INCREMENT PRIMARY KEY,
    member_id   BIGINT    NOT NULL,
    point       INT       NOT NULL,
    is_cancelled BOOLEAN  NOT NULL DEFAULT FALSE,
    played_at   DATE      NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_roulette_member FOREIGN KEY (member_id) REFERENCES members(id),
    CONSTRAINT uk_roulette_member_date UNIQUE (member_id, played_at)
);

CREATE INDEX idx_roulette_member_played ON roulette_histories(member_id, played_at);
