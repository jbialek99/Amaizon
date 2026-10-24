package org.example.amaizon.model.dtos;

public record CartItemDto(
        ProductDto product,
        int quantity
) {
}
