--liquibase formatted sql

--changeset DmitryLianguzov:3
CREATE TABLE product_purchase_stats (
    product_id BIGINT PRIMARY KEY,
    total_orders BIGINT NOT NULL DEFAULT 0,
    total_quantity_sold BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE processed_order_events (
    order_id BIGINT PRIMARY KEY,
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
