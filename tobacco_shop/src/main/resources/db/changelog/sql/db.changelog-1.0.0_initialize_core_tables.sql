--liquibase formatted sql

--changeset DmitryLianguzov:1
CREATE TABLE category (
                          id SERIAL PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          parent_category_id INTEGER,
                          CONSTRAINT fk_category_parent FOREIGN KEY (parent_category_id) REFERENCES category(id)
);

CREATE TABLE product (
                         id SERIAL PRIMARY KEY,
                         name VARCHAR(255) NOT NULL,
                         description TEXT,
                         stock_quantity INTEGER NOT NULL CHECK (stock_quantity >= 0)
);

CREATE TABLE product_category (
                                  id SERIAL PRIMARY KEY,
                                  product_id INTEGER NOT NULL,
                                  category_id INTEGER NOT NULL,
                                  CONSTRAINT fk_product_category_product FOREIGN KEY (product_id) REFERENCES product(id),
                                  CONSTRAINT fk_product_category_category FOREIGN KEY (category_id) REFERENCES category(id),
                                  CONSTRAINT uk_product_category_unique UNIQUE (product_id, category_id)
);

CREATE TABLE actual_price (
                              id SERIAL PRIMARY KEY,
                              product_id INTEGER NOT NULL UNIQUE,
                              price INTEGER NOT NULL CHECK (price >= 0),
                              CONSTRAINT fk_actual_price_product FOREIGN KEY (product_id) REFERENCES product(id)
);

CREATE TABLE discount (
                          id SERIAL PRIMARY KEY,
                          product_id INTEGER NOT NULL,
                          start_date TIMESTAMP NOT NULL,
                          end_date TIMESTAMP NOT NULL,
                          actual_price_id INTEGER NOT NULL,
                          CONSTRAINT fk_discount_product FOREIGN KEY (product_id) REFERENCES product(id),
                          CONSTRAINT fk_discount_actual_price FOREIGN KEY (actual_price_id) REFERENCES actual_price(id)
);

CREATE TABLE customer_order (
                                id SERIAL PRIMARY KEY,
                                total_sum INTEGER NOT NULL CHECK (total_sum >= 0),
                                status VARCHAR(50) NOT NULL
);

CREATE TABLE order_product (
                               id SERIAL PRIMARY KEY,
                               order_id INTEGER NOT NULL,
                               product_id INTEGER NOT NULL,
                               quantity INTEGER NOT NULL CHECK (quantity > 0),
                               purchase_price INTEGER NOT NULL CHECK (purchase_price >= 0),
                               CONSTRAINT fk_order_product_order FOREIGN KEY (order_id) REFERENCES customer_order(id),
                               CONSTRAINT fk_order_product_product FOREIGN KEY (product_id) REFERENCES product(id),
                               CONSTRAINT uk_order_product_unique UNIQUE (order_id, product_id)
);

-- TODO: Maybe add later in further migrations
-- CREATE INDEX idx_category_parent_id ON category(parent_category_id);
-- CREATE INDEX idx_discount_dates ON discount(start_date, end_date);
-- CREATE INDEX idx_product_name ON product(name);
-- CREATE INDEX idx_order_status ON customer_order(status);
-- CREATE INDEX idx_product_category_product_id ON product_category(product_id);
-- CREATE INDEX idx_product_category_category_id ON product_category(category_id);
