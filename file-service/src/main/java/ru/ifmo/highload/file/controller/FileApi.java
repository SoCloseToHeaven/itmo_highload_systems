package ru.ifmo.highload.file.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.ifmo.highload.file.dto.FileInfo;

@Tag(name = "File", description = "Product photos and file storage. Access: SUPERVISOR (all files), LOGISTICIAN (product photos), USER (read product photos only)")
public interface FileApi {

    @Operation(summary = "Upload product photo", description = "Upload photo for a product. Requires LOGISTICIAN or SUPERVISOR.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File successfully uploaded"),
            @ApiResponse(responseCode = "400", description = "Invalid file (empty, wrong type). Allowed: JPEG, PNG, GIF, WEBP"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden – requires LOGISTICIAN or SUPERVISOR"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PostMapping(value = "/product/{productId}/photo", consumes = "multipart/form-data")
    ResponseEntity<FileInfo> uploadProductPhoto(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Parameter(description = "Image file (JPEG, PNG, GIF, WEBP)") @RequestParam("file") MultipartFile file,
            Authentication authentication);

    @Operation(summary = "Upload internal file", description = "Upload internal file. Requires SUPERVISOR only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File successfully uploaded"),
            @ApiResponse(responseCode = "400", description = "Invalid file"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden – requires SUPERVISOR")
    })
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    ResponseEntity<FileInfo> uploadFile(
            @Parameter(description = "Image file") @RequestParam("file") MultipartFile file,
            Authentication authentication);

    @Operation(summary = "Get file content", description = "Download file. Product photos: any user. Internal files: SUPERVISOR only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File content"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden – internal file, requires SUPERVISOR"),
            @ApiResponse(responseCode = "404", description = "File not found")
    })
    @GetMapping("/{id}")
    ResponseEntity<InputStreamResource> getFile(
            @Parameter(description = "File ID") @PathVariable Long id);

    @Operation(summary = "Get file metadata", description = "Same access rules as GET /{id}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File metadata"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "File not found")
    })
    @GetMapping("/{id}/info")
    ResponseEntity<FileInfo> getFileInfo(
            @Parameter(description = "File ID") @PathVariable Long id);

    @Operation(summary = "List product photos", description = "Get paginated list of photos for a product. Any user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product photos"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/product/{productId}")
    ResponseEntity<Page<FileInfo>> getProductPhotos(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Parameter(description = "Pagination parameters", example = """
                    {
                      "page": 0,
                      "size": 1,
                      "sort": "string"
                    }""")             Pageable pageable);

    @Operation(summary = "List all files", description = "Get paginated list of all files. Requires SUPERVISOR only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All files"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden – requires SUPERVISOR")
    })
    @GetMapping
    ResponseEntity<Page<FileInfo>> getAllFiles(
            @Parameter(description = "Pagination parameters", example = """
                    {
                      "page": 0,
                      "size": 1,
                      "sort": "string"
                    }""")             Pageable pageable);

    @Operation(summary = "Delete file", description = "Product photos: LOGISTICIAN or SUPERVISOR. Internal files: SUPERVISOR only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "File deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "File not found")
    })
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteFile(
            @Parameter(description = "File ID") @PathVariable Long id);
}
