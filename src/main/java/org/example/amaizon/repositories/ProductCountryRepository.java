package org.example.amaizon.repositories;

import jakarta.persistence.LockModeType;
import org.example.amaizon.model.dtos.ProductDto;
import org.example.amaizon.model.entity.ProductCountry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductCountryRepository extends JpaRepository<ProductCountry, Long> {


    @Query("""
        select pc
        from ProductCountry pc
        join fetch pc.product p
        join fetch pc.translation t
        join fetch pc.price pr
        where pc.stableId = :productCountryStableId
          and pc.status = org.example.amaizon.model.enums.ProductCountryStatus.ACTIVE
          and pr.status = org.example.amaizon.model.enums.ProductPriceStatus.CURRENT
          and (pr.validTo is null or pr.validTo > current_timestamp)
        """)
    Optional<ProductCountry> findEntityByProductCountryStableId(
            @Param("productCountryStableId") UUID productCountryStableId
    );

    @Query("""
            select new org.example.amaizon.model.dtos.ProductDto(
                    p.stableId,
                    pc.stableId,
                    p.brand.value,
                    p.sku.value,
                    p.imageUrl.value,
                    t.displayName.value,
                    pr.price.value,
                    ps.quantityAvailable
            )
            from ProductStock ps
            join ps.productCountry pc
            join pc.product p
            join pc.country c
            join pc.translation t
            join pc.price pr
            where c.stableId = :countryStableId
              and pc.status = org.example.amaizon.model.enums.ProductCountryStatus.ACTIVE
              and pr.status = org.example.amaizon.model.enums.ProductPriceStatus.CURRENT
              and(pr.validTo is null or pr.validTo > current_timestamp )
            order by pc.id desc
            """)
    List<ProductDto> findProductsCountryByCountryStableId(
            @Param("countryStableId") UUID countryStableId
    );


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
                select pc
                from ProductCountry pc
                join pc.product p
                where p.stableId = :productStableId
                and pc.country.id = :countryId
            """)
    Optional<ProductCountry> findForUpdateByProductStableIdAndCountryId(
            @Param("productStableId") UUID productStableId,
            @Param("countryId") long countryId
    );


    @Query("""
            select p.stableId
            from ProductCountry pc
            join pc.product p
            where pc.country.id = :countryId
            """)
    List<UUID> findProductStableIdsByCountryId(@Param("countryId") long countryId);


    @Query("""
            select new org.example.amaizon.model.dtos.ProductDto(
                    p.stableId,
                    pc.stableId,
                    p.brand.value,
                    p.sku.value,
                    p.imageUrl.value,
                    t.displayName.value,
                    pr.price.value,
                    ps.quantityAvailable
            )
            from ProductStock ps
            join ps.productCountry pc
            join pc.product p
            join pc.country c
            join pc.translation t
            join pc.price pr
            where pc.stableId = :productCountryStableId
              and pc.status = org.example.amaizon.model.enums.ProductCountryStatus.ACTIVE
              and pr.status = org.example.amaizon.model.enums.ProductPriceStatus.CURRENT
              and(pr.validTo is null or pr.validTo > current_timestamp )
            order by pc.id desc
            """)
    Optional<ProductDto> findProductCountryDetailsByStableId(
            @Param("productCountryStableId") UUID productCountryStableId

    );

    @Query("""
select ProductCountry
from ProductCountry pc
where pc.stableId = :productCountryStableId
""")
  Optional<ProductCountry> findByStableId(@Param("productCountryStableId") UUID productCountryStableId);


}
