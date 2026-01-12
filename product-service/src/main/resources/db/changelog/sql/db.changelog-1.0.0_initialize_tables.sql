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

