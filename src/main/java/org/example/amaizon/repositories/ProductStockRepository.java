package org.example.amaizon.repositories;

import org.example.amaizon.model.entity.ProductCountry;
import org.example.amaizon.model.entity.ProductStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;


public interface ProductStockRepository extends JpaRepository<ProductStock, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select ps
           from ProductStock ps
           where ps.productCountry = :pc
           """)
    Optional<ProductStock> findByProductCountryForUpdate(@Param("pc") ProductCountry pc);

    Optional<ProductStock> findByProductCountry(ProductCountry productCountry);
}
