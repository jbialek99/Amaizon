package org.example.amaizon.model.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public class ProductDto {


    private final UUID productStableId;
    private final UUID productCountryStableId;
    private final String brand;
    private final String sku;
    private final String imageUrl;
    private final String name;
    private final BigDecimal price;
    private final long stock;

    public ProductDto(UUID productStableId,
                      UUID productCountryStableId,
                      String brand,
                      String sku,
                      String imageUrl,
                      String name,
                      BigDecimal price,
                      long stock) {
        this.productStableId = productStableId;
        this.productCountryStableId = productCountryStableId;
        this.brand = brand;
        this.sku = sku;
        this.imageUrl = imageUrl;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }


    public UUID productCountryStableId() { return productCountryStableId; }
    public UUID productStableId() { return productStableId; }
    public String brand() { return brand; }
    public String globalCode() { return sku; }
    public String imageUrl() { return imageUrl; }
    public String name() { return name; }
    public BigDecimal price() { return price; }
    public long stock() { return stock; }
}
