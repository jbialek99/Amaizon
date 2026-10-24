package org.example.amaizon.model;

import org.example.amaizon.exceptions.ProductValidationException;
import org.example.amaizon.model.enums.ProductCountryStatus;
import org.example.amaizon.model.enums.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class ProductTest {

    private Product product;
    private Category category;
    private Country country;

    @BeforeEach
    void setUp() {
        product = Product.createProduct("Nike", "GPC001", "image.jpg");
        category = new Category("Shoes.pl"," shoes","shoes", 5);
        country = Country.create("Poland", "PL", "pl", Currency.create("pln", "PLN", "zł"));
    }

    @Test
    void shouldCreateProductCorrectly() {
        assertThat(product.getBrand()).isEqualTo("Nike");
        assertThat(product.getGlobalCode()).isEqualTo("GPC001");
        assertThat(product.getImageUrl()).isEqualTo("image.jpg");
        assertThat(product.getStatus()).isEqualTo(ProductStatus.DRAFT);
        assertThat(product.getStableId()).isNotNull();
    }

    @Test
    void shouldChangeStatusToActiveIfCategoryExists() {
        product.changeCategory(category);
        product.changeStatus(ProductStatus.ACTIVE);

        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test
    void shouldThrowExceptionWhenActivatingWithoutCategory() {
        assertThatThrownBy(() -> product.changeStatus(ProductStatus.ACTIVE))
                .isInstanceOf(ProductValidationException.class)
                .hasMessage("validation.category.activate_without_category");
    }

    @Test
    void shouldChangeBrand() {
        product.rebrand("Adidas");
        assertThat(product.getBrand()).isEqualTo("Adidas");
    }

    @Test
    void shouldChangeImageUrl() {
        product.changeImageUrl("new_image.jpg");
        assertThat(product.getImageUrl()).isEqualTo("new_image.jpg");
    }

    @Test
    void shouldChangeGlobalCode() {
        product.changeGlobalCode("GPC002");
        assertThat(product.getGlobalCode()).isEqualTo("GPC002");
    }

    @Test
    void shouldAssignCountryToProduct() {
        ProductCountry pc = product.assignCountry(country, null);

        assertThat(pc.getCountry()).isEqualTo(country);
        assertThat(pc.getProduct()).isEqualTo(product);
        assertThat(pc.getStatus()).isEqualTo(ProductCountryStatus.DRAFT);
        assertThat(product.getProductCountries()).contains(pc);
    }

    @Test
    void shouldThrowExceptionWhenAssigningSameCountryTwice() {
        product.assignCountry(country, null);

        assertThatThrownBy(() -> product.assignCountry(country, null))
                .isInstanceOf(ProductValidationException.class)
                .hasMessage("validation.product.country.already_assigned");
    }

    @Test
    void shouldChangeCategory() {
        product.changeCategory(category);

        assertThat(product.getCategory()).isEqualTo(category);
    }

    @Test
    void equalityShouldBeBasedOnStableId() {
        Product product2 = Product.createProduct("Nike", "GPC001", "image.jpg");

        assertThat(product).isNotEqualTo(product2);

        Product product3 = new Product("Nike", "GPC001", "image.jpg") {
            {
                this.stableId = product.getStableId();
            }
        };

        assertThat(product).isEqualTo(product3);
    }
}
