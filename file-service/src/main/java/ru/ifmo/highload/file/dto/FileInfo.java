package ru.ifmo.highload.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;

@Data
@Schema(description = "File metadata")
public class FileInfo {

    @Schema(description = "File ID")
    private Long id;
    @Schema(description = "Product ID (null for internal files)")
    private Long productId;
    @Schema(description = "Original filename")
    private String filename;
    @Schema(description = "Content type")
    private String contentType;
    @Schema(description = "Upload timestamp")
    private Instant uploadedAt;

    public boolean isProductPhoto() {
        return productId != null;
    }
}
