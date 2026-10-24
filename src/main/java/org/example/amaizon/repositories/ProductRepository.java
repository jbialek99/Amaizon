package org.example.amaizon.repositories;

import org.example.amaizon.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByStableId(UUID stableId);
}
