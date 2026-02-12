--liquibase formatted sql

--changeset lab3:1
ALTER TABLE customer_order ADD COLUMN user_id BIGINT;

CREATE INDEX idx_customer_order_user_id ON customer_order(user_id);
