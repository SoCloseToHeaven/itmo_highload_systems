package ru.ifmo.highload.file.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class FileInfo {

    private Long id;
    private Long productId;
    private String filename;
    private String contentType;
    private Instant uploadedAt;

    public boolean isProductPhoto() {
        return productId != null;
    }
}
