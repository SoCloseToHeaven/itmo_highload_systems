--liquibase formatted sql
-- Пароль для supervisor по умолчанию: password (BCrypt hash)

--changeset auth:2
INSERT INTO app_user (username, password_hash, role, created_at) VALUES
('supervisor', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'SUPERVISOR', NOW());
