package org.example.amaizon.repositories;

import org.example.amaizon.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail_ValueIgnoreCase(String email);

    @Query("""
        select distinct u
        from User u
        left join fetch u.userRoles ur
        left join fetch ur.role r
        left join fetch r.country rc
        where lower(u.email.value) = lower(:email)
    """)
    Optional<User> findWithRolesByEmail(@Param("email") String email);

}
