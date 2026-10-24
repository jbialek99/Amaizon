package org.example.amaizon.repositories;

import org.example.amaizon.model.entity.Cart;
import org.example.amaizon.model.entity.Country;
import org.example.amaizon.model.entity.User;
import org.example.amaizon.model.enums.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cart c where c.id = :id")
    Optional<Cart> findByIdForUpdate(@Param("id") Long id);



    @Query("""
        select distinct c
        from Cart c
        left join fetch c.items i
        left join fetch i.productCountry pc
        left join fetch pc.product p
        left join fetch pc.translation t
        left join fetch pc.price pr
        where c.user = :user
          and c.country = :country
          and c.status = :status
    """)
    Optional<Cart>
    findActiveUserCartWithItems(
            @Param("user") User user,
            @Param("country") Country country,
            @Param("status") CartStatus status
    );

    @Query("""
        select distinct c
        from Cart c
        left join fetch c.items i
        left join fetch i.productCountry pc
        left join fetch pc.product p
        left join fetch pc.translation t
        left join fetch pc.price pr
        where c.guestToken = :guestToken
          and c.country = :country
          and c.status = :status
    """)
    Optional<Cart> findActiveGuestCartWithItems(
            @Param("guestToken") UUID guestToken,
            @Param("country") Country country,
            @Param("status") CartStatus status
    );
}
