package ru.ifmo.highload.file.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.ifmo.highload.file.dto.FileInfo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FileAccessServiceTest {

    @InjectMocks
    private FileAccessService fileAccessService;

    @BeforeEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void requireCanRead_Supervisor_AnyFile_ShouldPass() {
        setAuth(1L, "ROLE_SUPERVISOR");
        FileInfo fileInfo = new FileInfo();
        fileInfo.setProductId(null);

        assertDoesNotThrow(() -> fileAccessService.requireCanRead(fileInfo));
    }

    @Test
    void requireCanRead_User_ProductPhoto_ShouldPass() {
        setAuth(1L, "ROLE_USER");
        FileInfo fileInfo = new FileInfo();
        fileInfo.setProductId(10L);

        assertDoesNotThrow(() -> fileAccessService.requireCanRead(fileInfo));
    }

    @Test
    void requireCanRead_User_InternalFile_ShouldThrow() {
        setAuth(1L, "ROLE_USER");
        FileInfo fileInfo = new FileInfo();
        fileInfo.setProductId(null);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> fileAccessService.requireCanRead(fileInfo));

        assertTrue(ex.getMessage().contains("supervisor"));
    }

    @Test
    void requireCanUploadProductPhoto_Supervisor_ShouldPass() {
        setAuth(1L, "ROLE_SUPERVISOR");
        assertDoesNotThrow(() -> fileAccessService.requireCanUploadProductPhoto());
    }

    @Test
    void requireCanUploadProductPhoto_Logistician_ShouldPass() {
        setAuth(1L, "ROLE_LOGISTICIAN");
        assertDoesNotThrow(() -> fileAccessService.requireCanUploadProductPhoto());
    }

    @Test
    void requireCanUploadProductPhoto_User_ShouldThrow() {
        setAuth(1L, "ROLE_USER");

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> fileAccessService.requireCanUploadProductPhoto());

        assertTrue(ex.getMessage().contains("LOGISTICIAN") || ex.getMessage().contains("SUPERVISOR"));
    }

    @Test
    void requireCanUploadAnyFile_Supervisor_ShouldPass() {
        setAuth(1L, "ROLE_SUPERVISOR");
        assertDoesNotThrow(() -> fileAccessService.requireCanUploadAnyFile());
    }

    @Test
    void requireCanUploadAnyFile_Logistician_ShouldThrow() {
        setAuth(1L, "ROLE_LOGISTICIAN");

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> fileAccessService.requireCanUploadAnyFile());

        assertTrue(ex.getMessage().contains("SUPERVISOR"));
    }

    @Test
    void requireCanDelete_Supervisor_AnyFile_ShouldPass() {
        setAuth(1L, "ROLE_SUPERVISOR");
        FileInfo fileInfo = new FileInfo();
        fileInfo.setProductId(null);

        assertDoesNotThrow(() -> fileAccessService.requireCanDelete(fileInfo));
    }

    @Test
    void requireCanDelete_Logistician_ProductPhoto_ShouldPass() {
        setAuth(1L, "ROLE_LOGISTICIAN");
        FileInfo fileInfo = new FileInfo();
        fileInfo.setProductId(5L);

        assertDoesNotThrow(() -> fileAccessService.requireCanDelete(fileInfo));
    }

    @Test
    void requireCanDelete_Logistician_InternalFile_ShouldThrow() {
        setAuth(1L, "ROLE_LOGISTICIAN");
        FileInfo fileInfo = new FileInfo();
        fileInfo.setProductId(null);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> fileAccessService.requireCanDelete(fileInfo));

        assertTrue(ex.getMessage().contains("denied"));
    }

    @Test
    void requireCanDelete_User_ProductPhoto_ShouldThrow() {
        setAuth(1L, "ROLE_USER");
        FileInfo fileInfo = new FileInfo();
        fileInfo.setProductId(5L);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> fileAccessService.requireCanDelete(fileInfo));

        assertNotNull(ex.getMessage());
    }

    @Test
    void requireCanListAllFiles_Supervisor_ShouldPass() {
        setAuth(1L, "ROLE_SUPERVISOR");
        assertDoesNotThrow(() -> fileAccessService.requireCanListAllFiles());
    }

    @Test
    void requireCanListAllFiles_User_ShouldThrow() {
        setAuth(1L, "ROLE_USER");

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> fileAccessService.requireCanListAllFiles());

        assertTrue(ex.getMessage().contains("SUPERVISOR"));
    }

    private void setAuth(Long userId, String... roles) {
        List<SimpleGrantedAuthority> authorities = roles.length == 0
                ? Collections.emptyList()
                : Arrays.stream(roles)
                        .map(SimpleGrantedAuthority::new)
                        .toList();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userId, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
