--liquibase formatted sql
--changeset DmitryLianguzov:1

-- Таблица актуальных цен
CREATE TABLE actual_price (
                              id BIGSERIAL PRIMARY KEY,
                              product_id BIGINT NOT NULL UNIQUE,
                              price INTEGER NOT NULL CHECK (price >= 0),
                              created_at TIMESTAMP,
                              updated_at TIMESTAMP
);

-- Таблица скидок
CREATE TABLE discount (
                          id BIGSERIAL PRIMARY KEY,
                          product_id BIGINT NOT NULL,
                          start_date TIMESTAMP NOT NULL,
                          end_date TIMESTAMP NOT NULL,
                          actual_price_id BIGINT NOT NULL,
                          created_at TIMESTAMP,
                          updated_at TIMESTAMP,
                          CONSTRAINT fk_discount_actual_price FOREIGN KEY (actual_price_id) REFERENCES actual_price(id)
);

-- Индексы
CREATE INDEX idx_actual_price_product ON actual_price(product_id);
CREATE INDEX idx_discount_product ON discount(product_id);
CREATE INDEX idx_discount_dates ON discount(start_date, end_date);
CREATE INDEX idx_discount_actual_price ON discount(actual_price_id);

-- Базовые данные цен
INSERT INTO actual_price (product_id, price, created_at, updated_at) VALUES
                                                                         (1, 45000, NOW(), NOW()), (2, 35000, NOW(), NOW()), (3, 280000, NOW(), NOW()),
                                                                         (4, 320000, NOW(), NOW()), (5, 45000, NOW(), NOW()), (6, 40000, NOW(), NOW()),
                                                                         (7, 25000, NOW(), NOW()), (8, 22000, NOW(), NOW());

-- Пример скидки
INSERT INTO discount (product_id, start_date, end_date, actual_price_id, created_at, updated_at) VALUES
    (1, '2024-01-01 00:00:00', '2024-12-31 23:59:59', 1, NOW(), NOW());