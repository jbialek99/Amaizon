package org.example.amaizon.repositories;

import org.example.amaizon.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, Long> {



    @Query("""
    select o
    from Order o
    left join fetch o.items i
    left join fetch o.currency cur
    left join fetch o.country c
    left join fetch o.shipment s
    where o.stableId = :stableId
""")
    Optional<Order> findDetailsByStableId(@Param("stableId") UUID stableId);





}
