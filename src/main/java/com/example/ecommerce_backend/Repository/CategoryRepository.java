package com.example.ecommerce_backend.Repository;

import com.example.ecommerce_backend.Entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByActiveTrueOrderByNameAsc();

    Optional<Category> findByCodeIgnoreCase(String code);

    Optional<Category> findByNameIgnoreCase(String name);
}
