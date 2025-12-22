package com.junevi.baotao.repository;

import com.junevi.baotao.domain.Order;
import com.junevi.baotao.domain.OrderStatus;
import com.junevi.baotao.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUser(User user, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
}


