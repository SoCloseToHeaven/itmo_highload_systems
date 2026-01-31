--liquibase formatted sql

--changeset DmitryLianguzov:1
CREATE TABLE discount (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    actual_price_id BIGINT NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
    -- FK на product и actual_price убраны, так как они в других БД
);

