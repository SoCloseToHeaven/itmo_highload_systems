package ru.ifmo.highload.file.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface FileService {

    Long uploadProductPhoto(Long productId, MultipartFile file, Long userId);

    Long uploadFile(MultipartFile file, Long userId);

    FileInfo getFileInfo(Long id);

    InputStream getFileContent(Long id);

    Page<FileInfo> getProductPhotos(Long productId, Pageable pageable);

    Page<FileInfo> getAllFiles(Pageable pageable);

    void delete(Long id);
}
