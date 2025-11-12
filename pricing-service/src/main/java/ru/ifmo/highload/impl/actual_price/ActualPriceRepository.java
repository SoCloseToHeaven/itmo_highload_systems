package ru.ifmo.highload.impl.actual_price;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
interface ActualPriceRepository extends ReactiveCrudRepository<ActualPrice, Long> {

    Mono<ActualPrice> findByProductId(Long productId);

    Mono<Void> deleteByProductId(Long productId);

    @Query("SELECT EXISTS(SELECT 1 FROM actual_price WHERE product_id = :productId)")
    Mono<Boolean> existsByProductId(Long productId);
}
