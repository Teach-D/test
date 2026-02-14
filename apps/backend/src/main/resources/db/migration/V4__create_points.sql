CREATE TABLE points (
    id               BIGINT    AUTO_INCREMENT PRIMARY KEY,
    member_id        BIGINT    NOT NULL,
    amount           INT       NOT NULL,
    remaining_amount INT       NOT NULL,
    earned_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at       TIMESTAMP NOT NULL,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_point_member FOREIGN KEY (member_id) REFERENCES members(id)
);

CREATE INDEX idx_point_member ON points(member_id);
CREATE INDEX idx_point_expires ON points(expires_at);
CREATE INDEX idx_point_member_remaining ON points(member_id, remaining_amount);
