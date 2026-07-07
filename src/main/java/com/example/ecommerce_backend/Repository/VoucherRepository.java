package com.example.ecommerce_backend.Repository;

import com.example.ecommerce_backend.Entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByCode(String code);

    @Query("SELECT v FROM Voucher v WHERE v.active = true AND (v.expiryDate IS NULL OR v.expiryDate > :now) AND v.usedCount < v.maxUses")
    List<Voucher> findActiveVouchers(@Param("now") LocalDateTime now);
}
