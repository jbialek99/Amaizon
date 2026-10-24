package org.example.amaizon.repositories;

import org.example.amaizon.model.entity.Role;
import org.example.amaizon.model.enums.RoleKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByKey(RoleKey key);
}
