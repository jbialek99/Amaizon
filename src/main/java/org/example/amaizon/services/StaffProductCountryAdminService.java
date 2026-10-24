package org.example.amaizon.services;

import lombok.RequiredArgsConstructor;
import org.example.amaizon.model.dtos.CreateProductCountryForm;
import org.example.amaizon.security.StaffAuthorizationGuard;
import org.example.amaizon.model.dtos.MarketContextDto;
import org.example.amaizon.model.entity.Country;
import org.example.amaizon.model.entity.Product;
import org.example.amaizon.model.entity.ProductCountry;
import org.example.amaizon.model.entity.ProductStock;
import org.example.amaizon.model.enums.ProductCountryStatus;
import org.example.amaizon.model.enums.ProductPriceStatus;
import org.example.amaizon.repositories.CountryRepository;
import org.example.amaizon.repositories.ProductCountryRepository;
import org.example.amaizon.repositories.ProductRepository;
import org.example.amaizon.repositories.ProductStockRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StaffProductCountryAdminService {

    private final ProductRepository productRepository;
    private final CountryRepository countryRepository;
    private final ProductCountryRepository productCountryRepository;
    private final ProductStockRepository productStockRepository;
    private final StaffAuthorizationGuard authorizationGuard;

    public List<Product> listAllGlobalProducts(MarketContextDto market) {
        authorizationGuard.requireMarketProductAdmin(market);
        return productRepository.findAll();
    }

    public Product getGlobalProduct(UUID productStableId, MarketContextDto market) {
        authorizationGuard.requireMarketProductAdmin(market);
        return productRepository.findByStableId(productStableId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productStableId));
    }

    public CreateProductCountryForm buildDefaultForm(MarketContextDto market) {
        authorizationGuard.requireMarketProductAdmin(market);
        CreateProductCountryForm f = new CreateProductCountryForm();
        f.setInitialQuantityAvailable(0);
        return f;
    }


    public Set<UUID> findAlreadyAssignedProductStableIds(MarketContextDto market) {
        authorizationGuard.requireMarketProductAdmin(market);
        long countryId = market.getCountry().getId();
        return new HashSet<>(productCountryRepository.findProductStableIdsByCountryId(countryId));
    }


    @Transactional
    public ProductCountry createOrUpdateProductCountry(UUID productStableId, MarketContextDto market, CreateProductCountryForm form) {
        authorizationGuard.requireMarketProductAdmin(market);
        Product product = getGlobalProduct(productStableId, market);

        long countryId = market.getCountry().getId();
        Country country = countryRepository.findById(countryId)
                .orElseThrow(() -> new IllegalArgumentException("Country not found: " + countryId));

        ProductCountry pc = productCountryRepository
                .findForUpdateByProductStableIdAndCountryId(productStableId, countryId)
                .orElseGet(() -> product.assignCountry(country));

        String languageCode = market.getLocale() != null
                ? market.getLocale().toLanguageTag()
                : country.getLanguageCode().getValue();


        if (pc.getTranslation() == null) {
            pc.createTranslation(
                    form.getDisplayName(),
                    languageCode,
                    form.getSlug(),
                    form.getDescription()
            );
        } else {
            pc.updateTranslation(form.getDisplayName(), form.getSlug(), form.getDescription());
        }

        pc.createPrice(form.getPrice(), ProductPriceStatus.CURRENT, null);

        productStockRepository.findByProductCountryForUpdate(pc).ifPresentOrElse(
                existing -> {
                    long desired = form.getInitialQuantityAvailable();
                    long current = existing.getQuantityAvailable();
                    if (desired > current) {
                        existing.increaseAvailable((int) (desired - current));
                    }
                },
                () -> productStockRepository.save(ProductStock.create(pc, form.getInitialQuantityAvailable()))
        );

        if (form.getStatus() == ProductCountryStatus.ACTIVE) {
            pc.activateOrThrow();
        }

        try {
            productRepository.saveAndFlush(product);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("ProductCountry for this product and country already exists.");
        }

        return pc;
    }
}
