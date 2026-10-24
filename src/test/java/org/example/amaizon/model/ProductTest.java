package org.example.amaizon.model;

import org.example.amaizon.exceptions.validation.ProductValidationException;
import org.example.amaizon.model.entity.Product;
import org.example.amaizon.model.enums.ProductStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ProductTest {

    private static final String IMAGE_URL =
            "https://i.imgur.com/product.jpg";

    private Product createProduct() {
        return Product.createProduct(
                "Nike",
                "NK123456",
                IMAGE_URL
        );
    }

    @Test
    void shouldCreateProductInDraftStatus() {
        Product product = createProduct();

        assertThat(product.getBrand().getValue()).isEqualTo("Nike");
        assertThat(product.getSku().getValue()).isEqualTo("NK123456");
        assertThat(product.getImageUrl().getValue()).isEqualTo(IMAGE_URL);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.DRAFT);
        assertThat(product.getStableId()).isNotNull();
    }

    @Test
    void shouldChangeBrand() {
        Product product = createProduct();

        product.rebrand("Adidas");

        assertThat(product.getBrand().getValue()).isEqualTo("Adidas");
    }

    @Test
    void shouldChangeGlobalCode() {
        Product product = createProduct();

        product.changeGlobalCode("AD123456");

        assertThat(product.getSku().getValue()).isEqualTo("AD123456");
    }

    @Test
    void shouldChangeImageUrl() {
        Product product = createProduct();
        String newUrl = "https://i.imgur.com/updated.png";

        product.changeImageUrl(newUrl);

        assertThat(product.getImageUrl().getValue()).isEqualTo(newUrl);
    }

    @Test
    void shouldRejectActivationWithoutCategory() {
        Product product = createProduct();

        assertThatThrownBy(
                () -> product.changeStatus(ProductStatus.ACTIVE)
        ).isInstanceOf(ProductValidationException.class);

        assertThat(product.getStatus()).isEqualTo(ProductStatus.DRAFT);
    }

    @Test
    void shouldRejectNullCategory() {
        Product product = createProduct();

        assertThatThrownBy(
                () -> product.defineCategory(null)
        ).isInstanceOf(ProductValidationException.class);
    }

    @Test
    void shouldRejectNullStatus() {
        Product product = createProduct();

        assertThatThrownBy(
                () -> product.changeStatus(null)
        ).isInstanceOf(ProductValidationException.class);
    }

    @Test
    void shouldHaveDifferentIdentityForDifferentProducts() {
        Product first = createProduct();
        Product second = createProduct();

        assertThat(first).isNotEqualTo(second);
        assertThat(first.getStableId()).isNotEqualTo(second.getStableId());
    }

    @Test
    void shouldNotBeSellableInDraftStatus() {
        Product product = createProduct();

        assertThat(product.isSellable()).isFalse();
    }
}
