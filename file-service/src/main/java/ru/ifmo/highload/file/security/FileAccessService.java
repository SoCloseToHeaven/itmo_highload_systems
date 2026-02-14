package ru.ifmo.highload.file.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.ifmo.highload.file.dto.FileInfo;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FileAccessService {

    private static final String ROLE_PREFIX = "ROLE_";
    private static final String SUPERVISOR = "SUPERVISOR";
    private static final String LOGISTICIAN = "LOGISTICIAN";

    public void requireCanRead(FileInfo fileInfo) {
        if (hasRole(SUPERVISOR)) {
            return;
        }
        if (fileInfo.isProductPhoto()) {
            return;
        }
        throw new AccessDeniedException("Only supervisor can access this file");
    }

    public void requireCanUploadProductPhoto() {
        if (hasRole(SUPERVISOR) || hasRole(LOGISTICIAN)) {
            return;
        }
        throw new AccessDeniedException("Only LOGISTICIAN or SUPERVISOR can upload product photos");
    }

    public void requireCanUploadAnyFile() {
        if (hasRole(SUPERVISOR)) {
            return;
        }
        throw new AccessDeniedException("Only SUPERVISOR can upload internal files");
    }

    public void requireCanDelete(FileInfo fileInfo) {
        if (hasRole(SUPERVISOR)) {
            return;
        }
        if (fileInfo.isProductPhoto() && hasRole(LOGISTICIAN)) {
            return;
        }
        throw new AccessDeniedException("Access denied to delete this file");
    }

    public void requireCanListAllFiles() {
        if (hasRole(SUPERVISOR)) {
            return;
        }
        throw new AccessDeniedException("Only SUPERVISOR can list all files");
    }

    private boolean hasRole(String role) {
        Set<String> roles = getCurrentRoles();
        return roles.contains(role);
    }

    private Set<String> getCurrentRoles() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return Set.of();
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.startsWith(ROLE_PREFIX) ? a.substring(ROLE_PREFIX.length()) : a)
                .collect(Collectors.toSet());
    }
}
