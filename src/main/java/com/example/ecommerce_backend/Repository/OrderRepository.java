package com.example.ecommerce_backend.Repository;

import com.example.ecommerce_backend.Entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByOrderDateDesc(Long userID); //lich su mua hang cua Client

}
