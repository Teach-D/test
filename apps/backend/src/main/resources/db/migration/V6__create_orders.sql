CREATE TABLE orders (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    member_id   BIGINT       NOT NULL,
    product_id  BIGINT       NOT NULL,
    used_point  INT          NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'COMPLETED',
    ordered_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_member FOREIGN KEY (member_id) REFERENCES members(id),
    CONSTRAINT fk_order_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE INDEX idx_order_member ON orders(member_id);
