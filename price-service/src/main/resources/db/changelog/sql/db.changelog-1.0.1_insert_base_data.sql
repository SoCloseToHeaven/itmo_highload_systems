--liquibase formatted sql

--changeset DmitryLianguzov:2
INSERT INTO actual_price (product_id, price, created_at, updated_at) VALUES
                                                                         (1, 45000, NOW(), NOW()), (2, 35000, NOW(), NOW()), (3, 280000, NOW(), NOW()), (4, 320000, NOW(), NOW()),
                                                                         (5, 45000, NOW(), NOW()), (6, 40000, NOW(), NOW()), (7, 25000, NOW(), NOW()), (8, 22000, NOW(), NOW());

