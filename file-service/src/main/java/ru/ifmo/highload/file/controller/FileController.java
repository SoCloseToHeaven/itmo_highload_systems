package ru.ifmo.highload.file.controller;

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
public class FileController implements FileApi {

    private final FileService fileService;
    private final FileAccessService fileAccessService;

    @Override
    @PostMapping(value = "/product/{productId}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileInfo> uploadProductPhoto(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        fileAccessService.requireCanUploadProductPhoto();
        Long userId = (Long) authentication.getPrincipal();
        Long id = fileService.uploadProductPhoto(productId, file, userId);
        FileInfo info = fileService.getFileInfo(id);
        return ResponseEntity.ok(info);
    }

    @Override
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileInfo> uploadFile(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        fileAccessService.requireCanUploadAnyFile();
        Long userId = (Long) authentication.getPrincipal();
        Long id = fileService.uploadFile(file, userId);
        FileInfo info = fileService.getFileInfo(id);
        return ResponseEntity.ok(info);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<InputStreamResource> getFile(@PathVariable Long id) {
        FileInfo info = fileService.getFileInfo(id);
        fileAccessService.requireCanRead(info);
        InputStream content = fileService.getFileContent(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(info.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + info.getFilename() + "\"")
                .body(new InputStreamResource(content));
    }

    @Override
    @GetMapping("/{id}/info")
    public ResponseEntity<FileInfo> getFileInfo(@PathVariable Long id) {
        FileInfo info = fileService.getFileInfo(id);
        fileAccessService.requireCanRead(info);
        return ResponseEntity.ok(info);
    }

    @Override
    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<FileInfo>> getProductPhotos(
            @PathVariable Long productId,
            Pageable pageable) {
        return ResponseEntity.ok(fileService.getProductPhotos(productId, pageable));
    }

    @Override
    @GetMapping
    public ResponseEntity<Page<FileInfo>> getAllFiles(Pageable pageable) {
        fileAccessService.requireCanListAllFiles();
        return ResponseEntity.ok(fileService.getAllFiles(pageable));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long id) {
        FileInfo info = fileService.getFileInfo(id);
        fileAccessService.requireCanRead(info);
        fileAccessService.requireCanDelete(info);
        fileService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
