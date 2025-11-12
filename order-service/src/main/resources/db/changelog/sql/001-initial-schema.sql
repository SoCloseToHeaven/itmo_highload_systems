--liquibase formatted sql
--changeset DmitryLianguzov:1

-- Таблица заказов
CREATE TABLE customer_order (
                                id BIGSERIAL PRIMARY KEY,
                                total_sum INTEGER NOT NULL CHECK (total_sum >= 0),
                                status VARCHAR(50) NOT NULL,
                                created_at TIMESTAMP,
                                updated_at TIMESTAMP
);

-- Таблица товаров в заказе (Many-to-Many с дополнительными полями)
CREATE TABLE order_product (
                               id BIGSERIAL PRIMARY KEY,
                               order_id BIGINT NOT NULL,
                               product_id BIGINT NOT NULL,
                               quantity INTEGER NOT NULL CHECK (quantity > 0),
                               purchase_price INTEGER NOT NULL CHECK (purchase_price >= 0),
                               created_at TIMESTAMP,
                               CONSTRAINT fk_order_product_order FOREIGN KEY (order_id) REFERENCES customer_order(id),
                               CONSTRAINT uk_order_product_unique UNIQUE (order_id, product_id)
);

-- Индексы
CREATE INDEX idx_order_status ON customer_order(status);
CREATE INDEX idx_order_created_at ON customer_order(created_at);
CREATE INDEX idx_order_product_order ON order_product(order_id);
CREATE INDEX idx_order_product_product ON order_product(product_id);

-- Базовые данные заказов
INSERT INTO customer_order (total_sum, status, created_at, updated_at) VALUES
                                                                           (80000, 'PENDING', NOW(), NOW()),
                                                                           (530000, 'PROCESSING', NOW(), NOW());

INSERT INTO order_product (order_id, product_id, quantity, purchase_price, created_at) VALUES
                                                                                           (1, 1, 1, 45000, NOW()),
                                                                                           (1, 2, 1, 35000, NOW()),
                                                                                           (2, 3, 1, 280000, NOW()),
                                                                                           (2, 4, 1, 250000, NOW());