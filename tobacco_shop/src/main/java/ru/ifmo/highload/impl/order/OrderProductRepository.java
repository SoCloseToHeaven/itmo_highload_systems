package ru.ifmo.highload.impl.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {
    List<OrderProduct> findByOrderId(Long orderId);

    List<OrderProduct> findByProductId(Long productId);

    void deleteByOrderId(Long orderId);

    boolean existsByOrderIdAndProductId(Long orderId, Long productId);
}
