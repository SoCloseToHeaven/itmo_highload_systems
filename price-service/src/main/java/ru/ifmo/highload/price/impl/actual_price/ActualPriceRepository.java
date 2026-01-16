package ru.ifmo.highload.price.impl.actual_price;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
interface ActualPriceRepository extends R2dbcRepository<ActualPrice, Long> {

    Mono<ActualPrice> findByProductId(Long productId);

    @Query("DELETE FROM actual_price WHERE product_id = :productId")
    Mono<Void> deleteByProductId(@Param("productId") Long productId);

    @Query("SELECT EXISTS(SELECT 1 FROM actual_price WHERE product_id = :productId)")
    Mono<Boolean> existsByProductId(@Param("productId") Long productId);

    @Query("SELECT EXISTS(SELECT 1 FROM actual_price WHERE id = :id)")
    Mono<Boolean> existsById(@Param("id") Long id);
}

