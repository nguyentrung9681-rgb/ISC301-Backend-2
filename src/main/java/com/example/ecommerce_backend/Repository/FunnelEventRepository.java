package com.example.ecommerce_backend.Repository;

import com.example.ecommerce_backend.Entity.FunnelEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FunnelEventRepository extends JpaRepository<FunnelEvent, Long> {

    @Query("SELECT COUNT(DISTINCT f.sessionId) FROM FunnelEvent f WHERE f.eventType = :eventType")
    long countUniqueSessionsByEventType(@Param("eventType") String eventType);
}
