package ru.ifmo.highload.product.impl.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    List<ProductCategory> findByProductId(Long productId);

    List<ProductCategory> findByCategoryId(Long categoryId);

    void deleteByProductIdAndCategoryId(Long productId, Long categoryId);

    boolean existsByProductIdAndCategoryId(Long productId, Long categoryId);
}

