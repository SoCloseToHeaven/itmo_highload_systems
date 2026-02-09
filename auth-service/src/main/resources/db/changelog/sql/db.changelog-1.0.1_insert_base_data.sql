--liquibase formatted sql
-- Пароль для supervisor по умолчанию: password (BCrypt hash)

--changeset auth:2
INSERT INTO app_user (username, password_hash, role, created_at) VALUES
('supervisor', '$2a$10$ljtcpYUjOp.2/gmmnDSSGevaHWZ0JC6SOq1g7Xgo0fdINo4NPlGsa', 'SUPERVISOR', NOW());
