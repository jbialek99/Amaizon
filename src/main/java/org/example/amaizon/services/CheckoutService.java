package org.example.amaizon.services;

import org.example.amaizon.model.embeddable.all.AddressSnapshot;
import org.example.amaizon.model.entity.*;
import org.example.amaizon.model.enums.Carrier;
import org.example.amaizon.model.enums.CustomerType;
import org.example.amaizon.repositories.CartRepository;
import org.example.amaizon.repositories.OrderRepository;
import org.example.amaizon.repositories.ProductStockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CheckoutService {

    private final CartRepository cartRepository;
    private final ProductStockRepository stockRepository;
    private final OrderRepository orderRepository;

    public CheckoutService(
            CartRepository cartRepository,
            ProductStockRepository stockRepository,
            OrderRepository orderRepository
    ) {
        this.cartRepository = cartRepository;
        this.stockRepository = stockRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Order checkout(
            Cart cart,
            User userOrNull,
            String contactEmail,
            AddressSnapshot shipping,
            AddressSnapshot billing,
            CustomerType billingCustomerType,
            String billingCompanyName,
            String billingTaxId,
            Carrier carrier
    ) {

        if (cart == null) throw new IllegalArgumentException("cart required");
        if (shipping == null) throw new IllegalArgumentException("shipping required");
        if (billing == null) throw new IllegalArgumentException("billing required");
        if (carrier == null) throw new IllegalArgumentException("carrier required");
        if (contactEmail == null || contactEmail.isBlank()) throw new IllegalArgumentException("contactEmail required");

        if (userOrNull != null && cart.getUser() == null) {
            cart.assignToUser(userOrNull);
        }

        CustomerType type = (billingCustomerType == null)
                ? CustomerType.INDIVIDUAL
                : billingCustomerType;

        for (CartItem item : cart.getItems()) {
            ProductStock stock = stockRepository
                    .findByProductCountryForUpdate(item.getProductCountry())
                    .orElseThrow();

            stock.reserve(item.getQuantity());
        }

        Order order = Order.create(
                cart.getUser(),
                cart.getCountry(),
                cart.getCurrency(),
                contactEmail,
                shipping,
                billing,
                type,
                billingCompanyName,
                billingTaxId
        );

        for (CartItem item : cart.getItems()) {
            ProductCountry pc = item.getProductCountry();

            order.addItem(
                    pc.getProduct(),
                    resolveName(pc),
                    resolveSku(pc.getProduct()),
                    pc.getPrice().getPrice().getValue(),
                    item.getQuantity()
            );
        }

        order.setShippingCost(resolveShippingCost(carrier));

        order.createShipment(carrier, "PENDING");

        cart.checkout();
        cartRepository.save(cart);

        return orderRepository.save(order);
    }

    private BigDecimal resolveShippingCost(Carrier carrier) {
        return switch (carrier) {
            case INPOST -> new BigDecimal("12.99");
            case DPD -> new BigDecimal("19.99");
            case DHL -> new BigDecimal("24.99");
            case GLS -> new BigDecimal("18.99");
        };
    }

    private String resolveName(ProductCountry pc) {
        return pc.getTranslation() != null
                ? pc.getTranslation().getDisplayName().getValue()
                : "Product";
    }

    private String resolveSku(Product p) {
        return p.getSku() != null
                ? p.getSku().getValue()
                : "UNKNOWN";
    }
}
