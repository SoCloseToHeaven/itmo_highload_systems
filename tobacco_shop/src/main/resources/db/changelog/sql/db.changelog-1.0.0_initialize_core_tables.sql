--liquibase formatted sql

--changeset DmitryLianguzov:1
CREATE TABLE category (
                          id BIGSERIAL PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          parent_category_id BIGINT,
                          created_at TIMESTAMP,
                          updated_at TIMESTAMP,
                          CONSTRAINT fk_category_parent FOREIGN KEY (parent_category_id) REFERENCES category(id)
);

CREATE TABLE product (
                         id BIGSERIAL PRIMARY KEY,
                         name VARCHAR(255) NOT NULL,
                         description TEXT,
                         stock_quantity INTEGER NOT NULL CHECK (stock_quantity >= 0),
                         created_at TIMESTAMP,
                         updated_at TIMESTAMP
);

CREATE TABLE product_category (
                                  id BIGSERIAL PRIMARY KEY,
                                  product_id BIGINT NOT NULL,
                                  category_id BIGINT NOT NULL,
                                  CONSTRAINT fk_product_category_product FOREIGN KEY (product_id) REFERENCES product(id),
                                  CONSTRAINT fk_product_category_category FOREIGN KEY (category_id) REFERENCES category(id),
                                  CONSTRAINT uk_product_category_unique UNIQUE (product_id, category_id)
);

CREATE TABLE actual_price (
                              id BIGSERIAL PRIMARY KEY,
                              product_id BIGINT NOT NULL UNIQUE,
                              price INTEGER NOT NULL CHECK (price >= 0),
                              created_at TIMESTAMP,
                              updated_at TIMESTAMP,
                              CONSTRAINT fk_actual_price_product FOREIGN KEY (product_id) REFERENCES product(id)
);

CREATE TABLE discount (
                          id BIGSERIAL PRIMARY KEY,
                          product_id BIGINT NOT NULL,
                          start_date TIMESTAMP NOT NULL,
                          end_date TIMESTAMP NOT NULL,
                          actual_price_id BIGINT NOT NULL,
                          created_at TIMESTAMP,
                          updated_at TIMESTAMP,
                          CONSTRAINT fk_discount_product FOREIGN KEY (product_id) REFERENCES product(id),
                          CONSTRAINT fk_discount_actual_price FOREIGN KEY (actual_price_id) REFERENCES actual_price(id)
);

CREATE TABLE customer_order (
                                id BIGSERIAL PRIMARY KEY,
                                total_sum INTEGER NOT NULL CHECK (total_sum >= 0),
                                status VARCHAR(50) NOT NULL,
                                created_at TIMESTAMP,
                                updated_at TIMESTAMP
);

CREATE TABLE order_product (
                               id BIGSERIAL PRIMARY KEY,
                               order_id BIGINT NOT NULL,
                               product_id BIGINT NOT NULL,
                               quantity INTEGER NOT NULL CHECK (quantity > 0),
                               purchase_price INTEGER NOT NULL CHECK (purchase_price >= 0),
                               created_at TIMESTAMP,
                               CONSTRAINT fk_order_product_order FOREIGN KEY (order_id) REFERENCES customer_order(id),
                               CONSTRAINT fk_order_product_product FOREIGN KEY (product_id) REFERENCES product(id),
                               CONSTRAINT uk_order_product_unique UNIQUE (order_id, product_id)
);