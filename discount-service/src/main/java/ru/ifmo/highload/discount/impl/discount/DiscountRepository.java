package ru.ifmo.highload.discount.impl.discount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.ZonedDateTime;
import java.util.List;

@Repository
interface DiscountRepository extends JpaRepository<Discount, Long> {

    List<Discount> findByProductIdAndStartDateBeforeAndEndDateAfter(
            Long productId, ZonedDateTime date, ZonedDateTime date2);

    List<Discount> findByEndDateAfter(ZonedDateTime date);

    List<Discount> findByStartDateBeforeAndEndDateAfter(ZonedDateTime start, ZonedDateTime end);
}

