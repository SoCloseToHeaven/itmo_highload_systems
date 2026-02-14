--liquibase formatted sql

--changeset file-service:1
CREATE TABLE file_metadata (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT,
    filename VARCHAR(512) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL UNIQUE,
    uploaded_at TIMESTAMP NOT NULL,
    uploaded_by BIGINT NOT NULL
);

CREATE INDEX idx_file_metadata_product_id ON file_metadata(product_id);
