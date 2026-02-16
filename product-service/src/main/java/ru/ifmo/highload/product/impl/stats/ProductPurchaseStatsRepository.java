package ru.ifmo.highload.product.impl.stats;

import org.springframework.data.jpa.repository.JpaRepository;

interface ProductPurchaseStatsRepository extends JpaRepository<ProductPurchaseStats, Long> {
}
