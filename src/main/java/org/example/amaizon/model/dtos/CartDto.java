package org.example.amaizon.model.dtos;

import java.util.List;
import java.util.UUID;

public record CartDto(
        UUID stableId,
        List<CartItemDto> items
) {
    public CartDto {
        items = items == null ? List.of() : List.copyOf(items);
    }
}
