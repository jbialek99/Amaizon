package org.example.amaizon.repositories;

import org.example.amaizon.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByStableId(UUID stableId);
}
