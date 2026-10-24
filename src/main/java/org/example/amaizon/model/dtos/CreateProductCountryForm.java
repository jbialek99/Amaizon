package org.example.amaizon.model.dtos;

import jakarta.validation.constraints.*;
import org.example.amaizon.model.enums.ProductCountryStatus;

import java.math.BigDecimal;

public class CreateProductCountryForm {

    @NotBlank(message = "{validation.product_country.name.is_empty}")
    @Size(min = 2, max = 120, message = "{validation.product_country.name.invalid_length}")
    private String displayName;

    @NotBlank(message = "{validation.product_country.description.is_empty}")
    @Size(min = 10, max = 4000, message = "{validation.product_country.description.invalid_length}")
    private String description;

    @Pattern(
            regexp = "^$|^[a-z0-9]+(?:-[a-z0-9]+)*$",
            message = "{validation.product_country.slug.invalid}"
    )
    @Size(max = 180, message = "{validation.product_country.slug.too_long}")
    private String slug;

    @NotNull(message = "{validation.product_country.price.is_null}")
    @DecimalMin(value = "0.00", message = "{validation.product_country.price.min}")
    @Digits(integer = 12, fraction = 2, message = "{validation.product_country.price.invalid}")
    private BigDecimal price;

    @Min(value = 0, message = "{validation.product_country.stock.min}")
    private int initialQuantityAvailable;

    @NotNull(message = "{validation.product_country.status.is_null}")
    private ProductCountryStatus status = ProductCountryStatus.DRAFT;

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getInitialQuantityAvailable() { return initialQuantityAvailable; }
    public void setInitialQuantityAvailable(int initialQuantityAvailable) { this.initialQuantityAvailable = initialQuantityAvailable; }

    public ProductCountryStatus getStatus() { return status; }
    public void setStatus(ProductCountryStatus status) { this.status = status; }
}
