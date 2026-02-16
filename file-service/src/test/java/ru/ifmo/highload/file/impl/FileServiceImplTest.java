package ru.ifmo.highload.file.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.ifmo.highload.file.client.ProductServiceClient;
import ru.ifmo.highload.file.dto.FileInfo;
import ru.ifmo.highload.file.dto.external.ProductResponse;
import feign.FeignException;
import ru.ifmo.highload.file.impl.exceptions.BadRequestException;
import ru.ifmo.highload.file.impl.exceptions.ResourceNotFoundException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {

    @Mock
    private FileMetadataRepository fileMetadataRepository;

    @Mock
    private ProductServiceClient productServiceClient;

    @InjectMocks
    @Spy
    private FileServiceImpl fileService;

    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("file-service-test");
        ReflectionTestUtils.setField(fileService, "storagePath", tempDir.toString());
    }

    @Test
    void uploadProductPhoto_WhenProductExists_ShouldSaveAndReturnId() {
        Long productId = 1L;
        Long userId = 10L;
        MultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "image-content".getBytes());

        ProductResponse product = new ProductResponse();
        product.setId(productId);

        FileMetadata savedMeta = new FileMetadata();
        savedMeta.setId(1L);
        savedMeta.setProductId(productId);
        savedMeta.setFilename("photo.jpg");
        savedMeta.setContentType("image/jpeg");
        savedMeta.setStoredFilename("uuid-123");
        savedMeta.setUploadedAt(Instant.now());
        savedMeta.setUploadedBy(userId);

        when(productServiceClient.getProductById(productId)).thenReturn(product);
        when(fileMetadataRepository.save(any(FileMetadata.class))).thenReturn(savedMeta);

        Long result = fileService.uploadProductPhoto(productId, file, userId);

        assertNotNull(result);
        assertEquals(1L, result);
        verify(productServiceClient).getProductById(productId);
        verify(fileMetadataRepository).save(any(FileMetadata.class));
    }

    @Test
    void uploadProductPhoto_WhenProductNotFound_ShouldThrow() {
        Long productId = 999L;
        MultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "content".getBytes());

        when(productServiceClient.getProductById(productId)).thenReturn(null);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> fileService.uploadProductPhoto(productId, file, 1L));

        assertTrue(ex.getMessage().contains("999"));
        verify(fileMetadataRepository, never()).save(any());
    }

    @Test
    void uploadProductPhoto_WhenFeignReturns404_ShouldThrow() {
        Long productId = 999L;
        MultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "content".getBytes());

        FeignException.NotFound feign404 = mock(FeignException.NotFound.class);
        when(productServiceClient.getProductById(productId)).thenThrow(feign404);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> fileService.uploadProductPhoto(productId, file, 1L));

        assertTrue(ex.getMessage().contains("999"));
        verify(fileMetadataRepository, never()).save(any());
    }

    @Test
    void uploadProductPhoto_WhenFileEmpty_ShouldThrow() {
        MultipartFile file = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);
        when(productServiceClient.getProductById(1L)).thenReturn(new ProductResponse());

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> fileService.uploadProductPhoto(1L, file, 1L));

        assertEquals("File is empty", ex.getMessage());
    }

    @Test
    void uploadProductPhoto_WhenContentTypeInvalid_ShouldThrow() {
        MultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", "content".getBytes());
        when(productServiceClient.getProductById(1L)).thenReturn(new ProductResponse());

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> fileService.uploadProductPhoto(1L, file, 1L));

        assertTrue(ex.getMessage().contains("Allowed types"));
    }

    @Test
    void uploadFile_WithValidImage_ShouldSave() {
        MultipartFile file = new MockMultipartFile("file", "img.png", "image/png", "png-content".getBytes());
        FileMetadata savedMeta = new FileMetadata();
        savedMeta.setId(2L);
        savedMeta.setProductId(null);
        savedMeta.setFilename("img.png");
        savedMeta.setStoredFilename("uuid-456");
        savedMeta.setUploadedAt(Instant.now());
        savedMeta.setUploadedBy(5L);

        when(fileMetadataRepository.save(any(FileMetadata.class))).thenReturn(savedMeta);

        Long result = fileService.uploadFile(file, 5L);

        assertEquals(2L, result);
        verify(fileMetadataRepository).save(argThat(m -> m.getProductId() == null));
    }

    @Test
    void getFileInfo_WhenExists_ShouldReturnInfo() {
        Long id = 1L;
        FileMetadata meta = new FileMetadata();
        meta.setId(id);
        meta.setProductId(10L);
        meta.setFilename("test.jpg");
        meta.setContentType("image/jpeg");
        meta.setStoredFilename("stored-uuid");
        meta.setUploadedAt(Instant.now());
        meta.setUploadedBy(1L);

        when(fileMetadataRepository.findById(id)).thenReturn(Optional.of(meta));

        FileInfo info = fileService.getFileInfo(id);

        assertNotNull(info);
        assertEquals(id, info.getId());
        assertEquals(10L, info.getProductId());
        assertEquals("test.jpg", info.getFilename());
        assertTrue(info.isProductPhoto());
    }

    @Test
    void getFileInfo_WhenNotExists_ShouldThrow() {
        when(fileMetadataRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> fileService.getFileInfo(999L));

        assertTrue(ex.getMessage().contains("999"));
    }

    @Test
    void getFileContent_WhenExists_ShouldReturnInputStream() throws IOException {
        Path storedFile = tempDir.resolve("stored-123");
        Files.writeString(storedFile, "file-content");

        FileMetadata meta = new FileMetadata();
        meta.setId(1L);
        meta.setStoredFilename("stored-123");

        when(fileMetadataRepository.findById(1L)).thenReturn(Optional.of(meta));

        try (InputStream is = fileService.getFileContent(1L)) {
            assertNotNull(is);
            byte[] bytes = is.readAllBytes();
            assertEquals("file-content", new String(bytes));
        }
    }

    @Test
    void getFileContent_WhenMetadataNotExists_ShouldThrow() {
        when(fileMetadataRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> fileService.getFileContent(999L));
    }

    @Test
    void getFileContent_WhenFileNotOnDisk_ShouldThrow() {
        FileMetadata meta = new FileMetadata();
        meta.setId(1L);
        meta.setStoredFilename("nonexistent-file-xyz");
        when(fileMetadataRepository.findById(1L)).thenReturn(Optional.of(meta));

        assertThrows(ResourceNotFoundException.class, () -> fileService.getFileContent(1L));
    }

    @Test
    void getProductPhotos_ShouldReturnPage() {
        FileMetadata meta = new FileMetadata();
        meta.setId(1L);
        meta.setProductId(5L);
        meta.setFilename("p.jpg");
        meta.setContentType("image/jpeg");
        meta.setUploadedAt(Instant.now());
        Pageable pageable = PageRequest.of(0, 10);
        Page<FileMetadata> page = new PageImpl<>(List.of(meta), pageable, 1);

        when(fileMetadataRepository.findByProductId(5L, pageable)).thenReturn(page);

        Page<FileInfo> result = fileService.getProductPhotos(5L, pageable);

        assertEquals(1, result.getNumberOfElements());
        assertEquals(5L, result.getContent().get(0).getProductId());
    }

    @Test
    void getAllFiles_ShouldReturnPage() {
        FileMetadata meta = new FileMetadata();
        meta.setId(1L);
        meta.setProductId(null);
        meta.setFilename("internal.jpg");
        meta.setContentType("image/jpeg");
        meta.setUploadedAt(Instant.now());
        Pageable pageable = PageRequest.of(0, 10);
        Page<FileMetadata> page = new PageImpl<>(List.of(meta), pageable, 1);

        when(fileMetadataRepository.findAll(pageable)).thenReturn(page);

        Page<FileInfo> result = fileService.getAllFiles(pageable);

        assertEquals(1, result.getNumberOfElements());
        assertNull(result.getContent().get(0).getProductId());
        assertFalse(result.getContent().get(0).isProductPhoto());
    }

    @Test
    void delete_WhenExists_ShouldDeleteMetadataAndFile() throws IOException {
        Path storedFile = tempDir.resolve("to-delete");
        Files.writeString(storedFile, "content");

        FileMetadata meta = new FileMetadata();
        meta.setId(1L);
        meta.setStoredFilename("to-delete");
        when(fileMetadataRepository.findById(1L)).thenReturn(Optional.of(meta));
        doNothing().when(fileMetadataRepository).delete(meta);

        fileService.delete(1L);

        verify(fileMetadataRepository).delete(meta);
        assertFalse(Files.exists(storedFile));
    }

    @Test
    void delete_WhenNotExists_ShouldThrow() {
        when(fileMetadataRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> fileService.delete(999L));
    }
}
