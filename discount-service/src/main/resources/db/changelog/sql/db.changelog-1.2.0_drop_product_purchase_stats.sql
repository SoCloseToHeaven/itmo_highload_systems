--liquibase formatted sql

--changeset DmitryLianguzov:3
DROP TABLE IF EXISTS processed_order_events;
DROP TABLE IF EXISTS product_purchase_stats;
