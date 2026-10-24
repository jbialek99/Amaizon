package org.example.amaizon.model.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.example.amaizon.model.validators.validation.ValidProductImageUrl;

import java.util.UUID;

@Getter
@Setter
public class CreateGlobalProductForm {

    @NotBlank(message = "{validation.product.brand.is_empty}")
    @Size(max = 150, message = "{validation.product.brand.too_long}")
    private String brand;

    @NotBlank(message = "{validation.product.sku.is_empty}")
    @Size(min = 8, max = 12, message = "{validation.product.sku.invalid_length}")
    @Pattern(
            regexp = "^(?:[A-Z]{1,3}[0-9]+|[0-9]+)$",
            message = "{validation.product.sku.invalid}"
    )
    private String globalCode;

    @NotBlank(message = "{validation.product.imageUrl.is_empty}")
    @ValidProductImageUrl
    private String imageUrl;

    @NotNull(message = "{validation.product.category.is_null}")
    private UUID categoryStableId;
}
