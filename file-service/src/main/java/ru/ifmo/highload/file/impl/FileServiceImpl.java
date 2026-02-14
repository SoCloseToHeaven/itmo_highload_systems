package ru.ifmo.highload.file.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.ifmo.highload.file.api.FileService;
import ru.ifmo.highload.file.client.ProductServiceClient;
import ru.ifmo.highload.file.dto.FileInfo;
import ru.ifmo.highload.file.dto.external.ProductResponse;
import ru.ifmo.highload.file.impl.exceptions.BadRequestException;
import ru.ifmo.highload.file.impl.exceptions.ResourceNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileMetadataRepository fileMetadataRepository;
    private final ProductServiceClient productServiceClient;

    @Value("${file.storage-path:/app/files}")
    private String storagePath;

    private static final String[] ALLOWED_IMAGE_TYPES = {
            "image/jpeg", "image/png", "image/gif", "image/webp"
    };

    @Override
    @Transactional
    public Long uploadProductPhoto(Long productId, MultipartFile file, Long userId) {
        ProductResponse product = productServiceClient.getProductById(productId);
        if (product == null) {
            throw new ResourceNotFoundException("Product not found: " + productId);
        }
        validateImageFile(file);
        return saveFile(file, userId, productId);
    }

    @Override
    @Transactional
    public Long uploadFile(MultipartFile file, Long userId) {
        validateImageFile(file);
        return saveFile(file, userId, null);
    }

    @Override
    @Transactional(readOnly = true)
    public FileInfo getFileInfo(Long id) {
        FileMetadata meta = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + id));
        return toFileInfo(meta);
    }

    @Override
    @Transactional(readOnly = true)
    public InputStream getFileContent(Long id) {
        FileMetadata meta = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + id));
        Path path = resolveStoredPath(meta.getStoredFilename());
        if (!Files.exists(path)) {
            throw new ResourceNotFoundException("File content not found: " + id);
        }
        try {
            return Files.newInputStream(path);
        } catch (IOException e) {
            throw new ResourceNotFoundException("Cannot read file: " + id, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FileInfo> getProductPhotos(Long productId, Pageable pageable) {
        return fileMetadataRepository.findByProductId(productId, pageable)
                .map(this::toFileInfo);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FileInfo> getAllFiles(Pageable pageable) {
        return fileMetadataRepository.findAll(pageable)
                .map(this::toFileInfo);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        FileMetadata meta = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + id));
        Path path = resolveStoredPath(meta.getStoredFilename());
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
        fileMetadataRepository.delete(meta);
    }

    private Long saveFile(MultipartFile file, Long userId, Long productId) {
        try {
            String storedFilename = UUID.randomUUID().toString();
            Path dir = Path.of(storagePath);
            Files.createDirectories(dir);
            Path target = dir.resolve(storedFilename);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            FileMetadata meta = new FileMetadata();
            meta.setProductId(productId);
            meta.setFilename(Optional.ofNullable(file.getOriginalFilename()).orElse("unnamed"));
            meta.setContentType(file.getContentType());
            meta.setStoredFilename(storedFilename);
            meta.setUploadedAt(Instant.now());
            meta.setUploadedBy(userId);

            meta = fileMetadataRepository.save(meta);
            return meta.getId();
        } catch (IOException e) {
            throw new BadRequestException("Failed to store file: " + e.getMessage());
        }
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new BadRequestException("Content type is required");
        }
        boolean allowed = false;
        for (String t : ALLOWED_IMAGE_TYPES) {
            if (t.equalsIgnoreCase(contentType)) {
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            throw new BadRequestException("Allowed types: JPEG, PNG, GIF, WEBP");
        }
    }

    private Path resolveStoredPath(String storedFilename) {
        return Path.of(storagePath).resolve(storedFilename);
    }

    private FileInfo toFileInfo(FileMetadata meta) {
        FileInfo info = new FileInfo();
        info.setId(meta.getId());
        info.setProductId(meta.getProductId());
        info.setFilename(meta.getFilename());
        info.setContentType(meta.getContentType());
        info.setUploadedAt(meta.getUploadedAt());
        return info;
    }
}
