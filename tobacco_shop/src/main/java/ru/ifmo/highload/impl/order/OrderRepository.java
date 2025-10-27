package ru.ifmo.highload.impl.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findAll(Pageable pageable);
    // Для 3-й лабы с пользователями добавим:
    // Page<Order> findByUserId(Long userId, Pageable pageable);
}
