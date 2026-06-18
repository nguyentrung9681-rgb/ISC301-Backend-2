package com.example.ecommerce_backend.Repository;

import com.example.ecommerce_backend.Entity.Role;
import com.example.ecommerce_backend.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    List<User> findByRole(Role role);
    Optional<User> findByEmail(String email);
    @Query("""
            SELECT u FROM User u
            WHERE u.role = :role
            AND (
                LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR u.phone LIKE CONCAT('%', :keyword, '%')
            )
            """)
    List<User> searchUsersByEmailOrPhone(
            @Param("keyword") String keyword,
            @Param("role") Role role
    );
}
