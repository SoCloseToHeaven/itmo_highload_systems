package ru.ifmo.highload.price.impl.actual_price;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
interface ActualPriceRepository extends R2dbcRepository<ActualPrice, Long> {

    @Query("SELECT * FROM actual_price WHERE product_id = :productId")
    Mono<ActualPrice> findByProductId(@Param("productId") Long productId);

    @Query("SELECT * FROM actual_price WHERE id = :id")
    Mono<ActualPrice> findById(@Param("id") Long id);

    @Query("DELETE FROM actual_price WHERE product_id = :productId")
    Mono<Void> deleteByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(*) FROM actual_price WHERE product_id = :productId")
    Mono<Long> countByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(*) FROM actual_price WHERE id = :id")
    Mono<Long> countById(@Param("id") Long id);

    @Query("DELETE FROM actual_price WHERE id = :id")
    Mono<Void> deleteById(@Param("id") Long id);
}

