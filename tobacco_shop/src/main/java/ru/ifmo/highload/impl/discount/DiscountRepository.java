package ru.ifmo.highload.impl.discount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
interface DiscountRepository extends JpaRepository<Discount, Long> {

    List<Discount> findByProductIdAndStartDateBeforeAndEndDateAfter(
            Long productId, LocalDateTime date, LocalDateTime date2);

    List<Discount> findByEndDateAfter(LocalDateTime date);

    List<Discount> findByStartDateBeforeAndEndDateAfter(LocalDateTime start, LocalDateTime end);
}
