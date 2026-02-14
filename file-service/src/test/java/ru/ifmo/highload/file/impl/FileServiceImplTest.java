package ru.ifmo.highload.file.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import ru.ifmo.highload.file.api.FileService;
import ru.ifmo.highload.file.client.ProductServiceClient;
import ru.ifmo.highload.file.dto.FileInfo;
import ru.ifmo.highload.file.dto.external.ProductResponse;
import ru.ifmo.highload.file.impl.exceptions.BadRequestException;
import ru.ifmo.highload.file.impl.exceptions.ResourceNotFoundException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {

    @Mock
    private FileMetadataRepository fileMetadataRepository;

    @Mock
    private ProductServiceClient productServiceClient;

    @InjectMocks
    private FileServiceImpl fileService;

    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("file-service-test");
        ReflectionTestUtils.setField(fileService, "storagePath", tempDir.toString());
    }

    @Test
    void uploadProductPhoto_whenProductExists_shouldSaveAndReturnId() {
        ProductResponse product = new ProductResponse();
        product.setId(1L);
        product.setName("Product");
        when(productServiceClient.getProductById(1L)).thenReturn(product);

        FileMetadata saved = new FileMetadata();
        saved.setId(1L);
        saved.setProductId(1L);
        saved.setFilename("photo.jpg");
        saved.setContentType("image/jpeg");
        saved.setStoredFilename("uuid-1");
        saved.setUploadedAt(java.time.Instant.now());
        saved.setUploadedBy(1L);
        when(fileMetadataRepository.save(any(FileMetadata.class))).thenReturn(saved);

        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "content".getBytes());

        Long id = fileService.uploadProductPhoto(1L, file, 1L);

        assertThat(id).isEqualTo(1L);
        verify(productServiceClient).getProductById(1L);
        verify(fileMetadataRepository).save(any(FileMetadata.class));
    }

    @Test
    void uploadProductPhoto_whenProductNotFound_shouldThrow() {
        when(productServiceClient.getProductById(999L)).thenReturn(null);

        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "content".getBytes());

        assertThatThrownBy(() -> fileService.uploadProductPhoto(999L, file, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void uploadProductPhoto_whenFileEmpty_shouldThrow() {
        when(productServiceClient.getProductById(1L)).thenReturn(new ProductResponse());
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> fileService.uploadProductPhoto(1L, file, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void uploadProductPhoto_whenFileNull_shouldThrow() {
        when(productServiceClient.getProductById(1L)).thenReturn(new ProductResponse());

        assertThatThrownBy(() -> fileService.uploadProductPhoto(1L, null, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void uploadProductPhoto_whenInvalidContentType_shouldThrow() {
        when(productServiceClient.getProductById(1L)).thenReturn(new ProductResponse());
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", "content".getBytes());

        assertThatThrownBy(() -> fileService.uploadProductPhoto(1L, file, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Allowed types");
    }

    @Test
    void uploadFile_shouldSaveWithoutProductId() {
        FileMetadata saved = new FileMetadata();
        saved.setId(2L);
        saved.setProductId(null);
        saved.setFilename("internal.png");
        saved.setContentType("image/png");
        saved.setStoredFilename("uuid-2");
        saved.setUploadedAt(java.time.Instant.now());
        saved.setUploadedBy(1L);
        when(fileMetadataRepository.save(any(FileMetadata.class))).thenReturn(saved);

        MockMultipartFile file = new MockMultipartFile("file", "internal.png", "image/png", "content".getBytes());

        Long id = fileService.uploadFile(file, 1L);

        assertThat(id).isEqualTo(2L);
        verify(fileMetadataRepository).save(any(FileMetadata.class));
    }

    @Test
    void getFileInfo_whenExists_shouldReturnInfo() {
        FileMetadata meta = new FileMetadata();
        meta.setId(1L);
        meta.setProductId(1L);
        meta.setFilename("photo.jpg");
        meta.setContentType("image/jpeg");
        meta.setStoredFilename("stored-uuid");
        meta.setUploadedAt(java.time.Instant.now());
        meta.setUploadedBy(1L);
        when(fileMetadataRepository.findById(1L)).thenReturn(Optional.of(meta));

        FileInfo info = fileService.getFileInfo(1L);

        assertThat(info.getId()).isEqualTo(1L);
        assertThat(info.getProductId()).isEqualTo(1L);
        assertThat(info.getFilename()).isEqualTo("photo.jpg");
        assertThat(info.isProductPhoto()).isTrue();
    }

    @Test
    void getFileContent_whenFileExists_shouldReturnInputStream() throws IOException {
        String storedName = "content-file-uuid";
        Path filePath = tempDir.resolve(storedName);
        Files.write(filePath, "image bytes".getBytes());

        FileMetadata meta = new FileMetadata();
        meta.setId(1L);
        meta.setStoredFilename(storedName);
        meta.setContentType("image/jpeg");
        when(fileMetadataRepository.findById(1L)).thenReturn(Optional.of(meta));

        try (java.io.InputStream is = fileService.getFileContent(1L)) {
            assertThat(new String(is.readAllBytes())).isEqualTo("image bytes");
        }
    }

    @Test
    void getFileContent_whenFileNotOnDisk_shouldThrow() {
        FileMetadata meta = new FileMetadata();
        meta.setId(1L);
        meta.setStoredFilename("nonexistent-uuid");
        when(fileMetadataRepository.findById(1L)).thenReturn(Optional.of(meta));

        assertThatThrownBy(() -> fileService.getFileContent(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("File content not found");
    }

    @Test
    void getFileInfo_whenNotExists_shouldThrow() {
        when(fileMetadataRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fileService.getFileInfo(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("File not found");
    }

    @Test
    void getProductPhotos_shouldReturnPage() {
        FileMetadata meta = new FileMetadata();
        meta.setId(1L);
        meta.setProductId(1L);
        meta.setFilename("photo.jpg");
        meta.setContentType("image/jpeg");
        meta.setStoredFilename("uuid");
        meta.setUploadedAt(java.time.Instant.now());
        meta.setUploadedBy(1L);
        Page<FileMetadata> page = new PageImpl<>(Collections.singletonList(meta));
        when(fileMetadataRepository.findByProductId(1L, Pageable.unpaged())).thenReturn(page);

        Page<FileInfo> result = fileService.getProductPhotos(1L, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getProductId()).isEqualTo(1L);
    }

    @Test
    void getAllFiles_shouldReturnPage() {
        FileMetadata meta = new FileMetadata();
        meta.setId(1L);
        meta.setProductId(null);
        meta.setFilename("file.png");
        meta.setContentType("image/png");
        meta.setStoredFilename("uuid");
        meta.setUploadedAt(java.time.Instant.now());
        meta.setUploadedBy(1L);
        Page<FileMetadata> page = new PageImpl<>(Collections.singletonList(meta));
        when(fileMetadataRepository.findAll(Pageable.unpaged())).thenReturn(page);

        Page<FileInfo> result = fileService.getAllFiles(Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).isProductPhoto()).isFalse();
    }

    @Test
    void delete_whenExists_shouldDelete() throws IOException {
        Path filePath = tempDir.resolve("stored-uuid");
        Files.write(filePath, "content".getBytes());

        FileMetadata meta = new FileMetadata();
        meta.setId(1L);
        meta.setStoredFilename("stored-uuid");
        when(fileMetadataRepository.findById(1L)).thenReturn(Optional.of(meta));

        fileService.delete(1L);

        verify(fileMetadataRepository).delete(meta);
        assertThat(Files.exists(filePath)).isFalse();
    }

    @Test
    void delete_whenNotExists_shouldThrow() {
        when(fileMetadataRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fileService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("File not found");
    }
}
