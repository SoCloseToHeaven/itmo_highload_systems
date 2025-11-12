package ru.ifmo.highload.impl.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ifmo.highload.dto.order.OrderStatus;

import java.util.Optional;

@Repository
interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findAll(Pageable pageable);

    Optional<Order> findById(Long id);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    boolean existsById(Long id);
}
