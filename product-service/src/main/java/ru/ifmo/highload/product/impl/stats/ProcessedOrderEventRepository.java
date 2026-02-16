package ru.ifmo.highload.product.impl.stats;

import org.springframework.data.jpa.repository.JpaRepository;

interface ProcessedOrderEventRepository extends JpaRepository<ProcessedOrderEvent, Long> {

    boolean existsByOrderId(Long orderId);
}
