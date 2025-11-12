package ru.ifmo.highload.impl.actual_price;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
interface ActualPriceRepository extends JpaRepository<ActualPrice, Long> {

    Optional<ActualPrice> findByProductId(Long productId);

    void deleteByProductId(Long productId);

    boolean existsByProductId(Long productId);
}
