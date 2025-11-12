--liquibase formatted sql
--changeset DmitryLianguzov:1

-- Таблица категорий
CREATE TABLE category (
                          id BIGSERIAL PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          parent_category_id BIGINT,
                          created_at TIMESTAMP,
                          updated_at TIMESTAMP,
                          CONSTRAINT fk_category_parent FOREIGN KEY (parent_category_id) REFERENCES category(id)
);

-- Таблица товаров
CREATE TABLE product (
                         id BIGSERIAL PRIMARY KEY,
                         name VARCHAR(255) NOT NULL,
                         description TEXT,
                         stock_quantity INTEGER NOT NULL CHECK (stock_quantity >= 0),
                         created_at TIMESTAMP,
                         updated_at TIMESTAMP
);

-- Связь товаров и категорий (Many-to-Many)
CREATE TABLE product_category (
                                  id BIGSERIAL PRIMARY KEY,
                                  product_id BIGINT NOT NULL,
                                  category_id BIGINT NOT NULL,
                                  CONSTRAINT fk_product_category_product FOREIGN KEY (product_id) REFERENCES product(id),
                                  CONSTRAINT fk_product_category_category FOREIGN KEY (category_id) REFERENCES category(id),
                                  CONSTRAINT uk_product_category_unique UNIQUE (product_id, category_id)
);

-- Индексы
CREATE INDEX idx_category_parent ON category(parent_category_id);
CREATE INDEX idx_product_category_product ON product_category(product_id);
CREATE INDEX idx_product_category_category ON product_category(category_id);

-- Базовые данные
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