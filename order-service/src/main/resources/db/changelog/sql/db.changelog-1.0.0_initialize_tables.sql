--liquibase formatted sql

--changeset DmitryLianguzov:1
CREATE TABLE customer_order (
    id BIGSERIAL PRIMARY KEY,
    total_sum INTEGER NOT NULL CHECK (total_sum >= 0),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE order_product (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    purchase_price INTEGER NOT NULL CHECK (purchase_price >= 0),
    created_at TIMESTAMP,
    CONSTRAINT fk_order_product_order FOREIGN KEY (order_id) REFERENCES customer_order(id) ON DELETE CASCADE
    -- FK на product убран, так как product в другой БД
);

