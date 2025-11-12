package ru.ifmo.highload.impl.discount;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import java.time.LocalDateTime;

@Repository
interface DiscountRepository extends ReactiveCrudRepository<Discount, Long> {

    @Query("SELECT * FROM discount WHERE product_id = :productId AND start_date <= :date AND end_date >= :date2")
    Flux<Discount> findByProductIdAndStartDateBeforeAndEndDateAfter(
            Long productId, LocalDateTime date, LocalDateTime date2);

    @Query("SELECT * FROM discount WHERE end_date > :date")
    Flux<Discount> findByEndDateAfter(LocalDateTime date);

    @Query("SELECT * FROM discount WHERE start_date <= :start AND end_date >= :end")
    Flux<Discount> findByStartDateBeforeAndEndDateAfter(LocalDateTime start, LocalDateTime end);
}
