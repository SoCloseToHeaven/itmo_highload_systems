package ru.ifmo.highload.file.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.ifmo.highload.file.dto.FileInfo;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileAccessServiceTest {

    private FileAccessService fileAccessService;

    @BeforeEach
    void setUp() {
        fileAccessService = new FileAccessService();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setAuth(String... roles) {
        List<SimpleGrantedAuthority> authorities = List.of(roles).stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .toList();
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(1L, null, authorities));
        SecurityContextHolder.setContext(context);
    }

    private FileInfo productPhotoInfo() {
        FileInfo info = new FileInfo();
        info.setId(1L);
        info.setProductId(1L);
        info.setFilename("photo.jpg");
        return info;
    }

    private FileInfo internalFileInfo() {
        FileInfo info = new FileInfo();
        info.setId(2L);
        info.setProductId(null);
        info.setFilename("internal.png");
        return info;
    }

    @Test
    void requireCanRead_supervisorCanReadAnyFile() {
        setAuth("SUPERVISOR");
        assertThatCode(() -> fileAccessService.requireCanRead(productPhotoInfo())).doesNotThrowAnyException();
        assertThatCode(() -> fileAccessService.requireCanRead(internalFileInfo())).doesNotThrowAnyException();
    }

    @Test
    void requireCanRead_userCanReadProductPhoto() {
        setAuth("USER");
        assertThatCode(() -> fileAccessService.requireCanRead(productPhotoInfo())).doesNotThrowAnyException();
    }

    @Test
    void requireCanRead_userCannotReadInternalFile() {
        setAuth("USER");
        assertThatThrownBy(() -> fileAccessService.requireCanRead(internalFileInfo()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Only supervisor");
    }

    @Test
    void requireCanUploadProductPhoto_logisticianAllowed() {
        setAuth("LOGISTICIAN");
        assertThatCode(() -> fileAccessService.requireCanUploadProductPhoto()).doesNotThrowAnyException();
    }

    @Test
    void requireCanUploadProductPhoto_supervisorAllowed() {
        setAuth("SUPERVISOR");
        assertThatCode(() -> fileAccessService.requireCanUploadProductPhoto()).doesNotThrowAnyException();
    }

    @Test
    void requireCanUploadProductPhoto_userDenied() {
        setAuth("USER");
        assertThatThrownBy(() -> fileAccessService.requireCanUploadProductPhoto())
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("LOGISTICIAN or SUPERVISOR");
    }

    @Test
    void requireCanUploadAnyFile_supervisorAllowed() {
        setAuth("SUPERVISOR");
        assertThatCode(() -> fileAccessService.requireCanUploadAnyFile()).doesNotThrowAnyException();
    }

    @Test
    void requireCanUploadAnyFile_logisticianDenied() {
        setAuth("LOGISTICIAN");
        assertThatThrownBy(() -> fileAccessService.requireCanUploadAnyFile())
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Only SUPERVISOR");
    }

    @Test
    void requireCanDelete_supervisorCanDeleteAny() {
        setAuth("SUPERVISOR");
        assertThatCode(() -> fileAccessService.requireCanDelete(productPhotoInfo())).doesNotThrowAnyException();
        assertThatCode(() -> fileAccessService.requireCanDelete(internalFileInfo())).doesNotThrowAnyException();
    }

    @Test
    void requireCanDelete_logisticianCanDeleteProductPhoto() {
        setAuth("LOGISTICIAN");
        assertThatCode(() -> fileAccessService.requireCanDelete(productPhotoInfo())).doesNotThrowAnyException();
    }

    @Test
    void requireCanDelete_logisticianCannotDeleteInternalFile() {
        setAuth("LOGISTICIAN");
        assertThatThrownBy(() -> fileAccessService.requireCanDelete(internalFileInfo()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Access denied");
    }

    @Test
    void requireCanDelete_userDenied() {
        setAuth("USER");
        assertThatThrownBy(() -> fileAccessService.requireCanDelete(productPhotoInfo()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Access denied");
    }

    @Test
    void requireCanListAllFiles_supervisorAllowed() {
        setAuth("SUPERVISOR");
        assertThatCode(() -> fileAccessService.requireCanListAllFiles()).doesNotThrowAnyException();
    }

    @Test
    void requireCanListAllFiles_userDenied() {
        setAuth("USER");
        assertThatThrownBy(() -> fileAccessService.requireCanListAllFiles())
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Only SUPERVISOR");
    }
}
