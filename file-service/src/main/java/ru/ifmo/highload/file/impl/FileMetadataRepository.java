package ru.ifmo.highload.file.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    Page<FileMetadata> findByProductId(Long productId, Pageable pageable);

    List<FileMetadata> findByProductId(Long productId);
}
