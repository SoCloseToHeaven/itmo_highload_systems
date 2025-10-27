package ru.ifmo.highload.impl.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
interface CategoryRepository extends JpaRepository<Category, Long> {

    Page<Category> findAll(Pageable pageable);

    List<Category> findByParentCategoryIdIsNull();

    boolean existsByNameAndParentCategoryId(String name, Long parentCategoryId);

    List<Category> findByParentCategoryId(Long parentId);
}