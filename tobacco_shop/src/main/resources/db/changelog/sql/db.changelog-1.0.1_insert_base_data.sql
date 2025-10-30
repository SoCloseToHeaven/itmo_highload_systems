--liquibase formatted sql

--changeset DmitryLianguzov:2
INSERT INTO category (name, parent_category_id, created_at, updated_at) VALUES
                                                                            ('Табачные изделия', NULL, NOW(), NOW()),
                                                                            ('Электронные сигареты', 1, NOW(), NOW()),
                                                                            ('Одноразовые электронные сигареты', 2, NOW(), NOW()),
                                                                            ('Многоразовые устройства', 2, NOW(), NOW()),
                                                                            ('Жидкости для вейпа', 1, NOW(), NOW()),
                                                                            ('Солевые жидкости', 5, NOW(), NOW()),
                                                                            ('Обычные жидкости', 5, NOW(), NOW()),
                                                                            ('Комплектующие', 1, NOW(), NOW()),
                                                                            ('Испарители', 8, NOW(), NOW()),
                                                                            ('Аккумуляторы', 8, NOW(), NOW());

INSERT INTO product (name, description, stock_quantity, created_at, updated_at) VALUES
                                                                                    ('HQD Crystal Plus', 'Одноразовая электронная сигарета 2500 тяг', 100, NOW(), NOW()),
                                                                                    ('Elf Bar 600', 'Одноразовая электронная сигарета 600 тяг', 150, NOW(), NOW()),
                                                                                    ('Vaporesso XROS 3', 'Многоразовая pod-система', 50, NOW(), NOW()),
                                                                                    ('Uwell Caliburn G2', 'Многоразовая pod-система с регулируемой тягой', 45, NOW(), NOW()),
                                                                                    ('SALT Brazilian Coffee 30мл', 'Солевая жидкость вкус бразильский кофе', 80, NOW(), NOW()),
                                                                                    ('FREEZZ Menthol Ice 30мл', 'Обычная жидкость вкус ментол лёд', 75, NOW(), NOW()),
                                                                                    ('Vaporesso GTX Coil 0.8 Ohm', 'Испаритель для устройств Vaporesso', 200, NOW(), NOW()),
                                                                                    ('Uwell Caliburn Coil 1.0 Ohm', 'Испаритель для Caliburn серии', 180, NOW(), NOW());

INSERT INTO product_category (product_id, category_id) VALUES
                                                           (1, 3), (2, 3), (3, 4), (4, 4), (5, 6), (6, 7), (7, 9), (8, 9);

INSERT INTO actual_price (product_id, price, created_at, updated_at) VALUES
                                                                         (1, 45000, NOW(), NOW()), (2, 35000, NOW(), NOW()), (3, 280000, NOW(), NOW()), (4, 320000, NOW(), NOW()),
                                                                         (5, 45000, NOW(), NOW()), (6, 40000, NOW(), NOW()), (7, 25000, NOW(), NOW()), (8, 22000, NOW(), NOW());

INSERT INTO customer_order (total_sum, status, created_at, updated_at) VALUES
                                                                           (80000, 'PENDING', NOW(), NOW()),
                                                                           (530000, 'PROCESSING', NOW(), NOW());

INSERT INTO order_product (order_id, product_id, quantity, purchase_price, created_at) VALUES
                                                                                           (1, 1, 1, 45000, NOW()),
                                                                                           (1, 2, 1, 35000, NOW()),
                                                                                           (2, 3, 1, 280000, NOW()),
                                                                                           (2, 4, 1, 250000, NOW());