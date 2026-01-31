--liquibase formatted sql

--changeset auth:1
CREATE TABLE app_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP
);

CREATE INDEX idx_app_user_username ON app_user(username);
