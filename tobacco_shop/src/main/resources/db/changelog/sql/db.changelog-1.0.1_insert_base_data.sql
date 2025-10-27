--liquibase formatted sql

--changeset DmitryLianguzov:2
INSERT INTO category (id, name, parent_category_id) VALUES
                                                        (1, 'Табачные изделия', NULL),
                                                        (2, 'Электронные сигареты', 1),
                                                        (3, 'Одноразовые электронные сигареты', 2),
                                                        (4, 'Многоразовые устройства', 2),
                                                        (5, 'Жидкости для вейпа', 1),
                                                        (6, 'Солевые жидкости', 5),
                                                        (7, 'Обычные жидкости', 5),
                                                        (8, 'Комплектующие', 1),
                                                        (9, 'Испарители', 8),
                                                        (10, 'Аккумуляторы', 8);

SELECT setval('category_id_seq', (SELECT MAX(id) FROM category));

INSERT INTO product (id, name, description, stock_quantity) VALUES
                                                                (1, 'HQD Crystal Plus', 'Одноразовая электронная сигарета 2500 тяг', 100),
                                                                (2, 'Elf Bar 600', 'Одноразовая электронная сигарета 600 тяг', 150),
                                                                (3, 'Vaporesso XROS 3', 'Многоразовая pod-система', 50),
                                                                (4, 'Uwell Caliburn G2', 'Многоразовая pod-система с регулируемой тягой', 45),
                                                                (5, 'SALT Brazilian Coffee 30мл', 'Солевая жидкость вкус бразильский кофе', 80),
                                                                (6, 'FREEZZ Menthol Ice 30мл', 'Обычная жидкость вкус ментол лёд', 75),
                                                                (7, 'Vaporesso GTX Coil 0.8 Ohm', 'Испаритель для устройств Vaporesso', 200),
                                                                (8, 'Uwell Caliburn Coil 1.0 Ohm', 'Испаритель для Caliburn серии', 180);

SELECT setval('product_id_seq', (SELECT MAX(id) FROM product));

INSERT INTO product_category (product_id, category_id) VALUES
                                                           (1, 3), (2, 3), (3, 4), (4, 4), (5, 6), (6, 7), (7, 9), (8, 9);

INSERT INTO actual_price (product_id, price) VALUES
                                                 (1, 45000), (2, 35000), (3, 280000), (4, 320000),
                                                 (5, 45000), (6, 40000), (7, 25000), (8, 22000);

INSERT INTO customer_order (id, total_sum, status) VALUES
                                                       (1, 80000, 'PENDING'),
                                                       (2, 530000, 'PROCESSING');

SELECT setval('customer_order_id_seq', (SELECT MAX(id) FROM customer_order));

INSERT INTO order_product (order_id, product_id, quantity, purchase_price) VALUES
                                                                               (1, 1, 1, 45000),
                                                                               (1, 2, 1, 35000),
                                                                               (2, 3, 1, 280000),
                                                                               (2, 4, 1, 250000);