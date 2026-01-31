--liquibase formatted sql

--changeset DmitryLianguzov:2
INSERT INTO customer_order (total_sum, status, created_at, updated_at) VALUES
                                                                           (80000, 'PENDING', NOW(), NOW()),
                                                                           (530000, 'PROCESSING', NOW(), NOW());

INSERT INTO order_product (order_id, product_id, quantity, purchase_price, created_at) VALUES
                                                                                           (1, 1, 1, 45000, NOW()),
                                                                                           (1, 2, 1, 35000, NOW()),
                                                                                           (2, 3, 1, 280000, NOW()),
                                                                                           (2, 4, 1, 250000, NOW());

