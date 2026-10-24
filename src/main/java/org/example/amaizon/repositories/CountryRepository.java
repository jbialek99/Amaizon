package org.example.amaizon.repositories;

import org.example.amaizon.model.entity.Country;
import org.example.amaizon.model.enums.DomainStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CountryRepository extends JpaRepository<Country, Long> {

    @Query("""
            select c
            from Country c
            join fetch c.domain d
            join fetch c.currency curr
            where lower(d.ccTld) = :tld
            and d.status = :status
            """)
    Optional<Country> findByDomainTldAndStatus(String tld, DomainStatus status);
}
