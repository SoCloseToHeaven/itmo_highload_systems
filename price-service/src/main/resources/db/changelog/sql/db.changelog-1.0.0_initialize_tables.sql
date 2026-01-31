--liquibase formatted sql

--changeset DmitryLianguzov:1
CREATE TABLE actual_price (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL UNIQUE,
    price INTEGER NOT NULL CHECK (price >= 0),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
    -- FK на product убран, так как product в другой БД
);

