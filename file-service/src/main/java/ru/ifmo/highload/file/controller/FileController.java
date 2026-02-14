package ru.ifmo.highload.file.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.ifmo.highload.file.api.FileService;
import ru.ifmo.highload.file.dto.FileInfo;
import ru.ifmo.highload.file.security.FileAccessService;

import java.io.InputStream;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
@Tag(name = "File", description = "Product photos and file storage")
public class FileController {

    private final FileService fileService;
    private final FileAccessService fileAccessService;

    @PostMapping(value = "/product/{productId}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload product photo (LOGISTICIAN, SUPERVISOR)")
    public ResponseEntity<FileInfo> uploadProductPhoto(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file,
            Authentication auth) {
        fileAccessService.requireCanUploadProductPhoto();
        Long userId = (Long) auth.getPrincipal();
        Long id = fileService.uploadProductPhoto(productId, file, userId);
        FileInfo info = fileService.getFileInfo(id);
        return ResponseEntity.ok(info);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload internal file (SUPERVISOR only)")
    public ResponseEntity<FileInfo> uploadFile(
            @RequestParam("file") MultipartFile file,
            Authentication auth) {
        fileAccessService.requireCanUploadAnyFile();
        Long userId = (Long) auth.getPrincipal();
        Long id = fileService.uploadFile(file, userId);
        FileInfo info = fileService.getFileInfo(id);
        return ResponseEntity.ok(info);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get file content. Product photos: any user. Other files: SUPERVISOR only")
    public ResponseEntity<InputStreamResource> getFile(@PathVariable Long id) {
        FileInfo info = fileService.getFileInfo(id);
        fileAccessService.requireCanRead(info);
        InputStream content = fileService.getFileContent(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(info.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + info.getFilename() + "\"")
                .body(new InputStreamResource(content));
    }

    @GetMapping("/{id}/info")
    @Operation(summary = "Get file metadata. Same access rules as GET /{id}")
    public ResponseEntity<FileInfo> getFileInfo(@PathVariable Long id) {
        FileInfo info = fileService.getFileInfo(id);
        fileAccessService.requireCanRead(info);
        return ResponseEntity.ok(info);
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "List product photos (any authenticated user)")
    public ResponseEntity<Page<FileInfo>> getProductPhotos(
            @PathVariable Long productId,
            Pageable pageable) {
        return ResponseEntity.ok(fileService.getProductPhotos(productId, pageable));
    }

    @GetMapping
    @Operation(summary = "List all files (SUPERVISOR only)")
    public ResponseEntity<Page<FileInfo>> getAllFiles(Pageable pageable) {
        fileAccessService.requireCanListAllFiles();
        return ResponseEntity.ok(fileService.getAllFiles(pageable));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete file. Product photos: LOGISTICIAN, SUPERVISOR. Other: SUPERVISOR only")
    public ResponseEntity<Void> deleteFile(@PathVariable Long id) {
        FileInfo info = fileService.getFileInfo(id);
        fileAccessService.requireCanRead(info);
        fileAccessService.requireCanDelete(info);
        fileService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
