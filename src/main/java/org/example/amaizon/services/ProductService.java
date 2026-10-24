package org.example.amaizon.services;

import org.example.amaizon.model.dtos.ProductDto;
import org.example.amaizon.repositories.ProductCountryRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductCountryRepository productCountryRepository;

    public ProductService(ProductCountryRepository productCountryRepository) {
        this.productCountryRepository = productCountryRepository;
    }


    public List<ProductDto> findHomeProducts(UUID countryUUID) {
        return productCountryRepository.findProductsCountryByCountryStableId(countryUUID);
    }

    public Optional<ProductDto> findProductDetailsById(UUID productCountryStableId) {
        return productCountryRepository.findProductCountryDetailsByStableId(productCountryStableId);
    }


}
