package com.example.ecommerce_backend.Repository;

import com.example.ecommerce_backend.Entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {

    @Query("SELECT ua FROM UserAddress ua WHERE ua.user.id = :userId ORDER BY ua.isDefault DESC, ua.id ASC")
    List<UserAddress> findByUserIdOrderByDefaultDesc(@Param("userId") Long userId);

    Optional<UserAddress> findByUserIdAndIsDefault(Long userId, Boolean isDefault);
}
