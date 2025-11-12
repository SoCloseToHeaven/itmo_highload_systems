package ru.ifmo.highload.impl.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.Optional;

@Repository
interface CategoryRepository extends JpaRepository<Category, Long> {
    Page<Category> findAll(Pageable pageable);

    Page<Category> findByParentCategoryIdIsNull(Pageable pageable);

    Optional<Category> findById(Long id);

    boolean existsByNameAndParentCategoryId(String name, Long parentCategoryId);

    List<Category> findByParentCategoryId(Long parentId);

    boolean existsById(Long id);
}