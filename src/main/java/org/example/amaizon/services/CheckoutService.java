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



@Service
public class CheckoutService {

    private final CartRepository cartRepository;
    private final ProductStockRepository stockRepository;
    private final OrderRepository orderRepository;
    private final ShippingPricing shippingPricing;

    public CheckoutService(
            CartRepository cartRepository,
            ProductStockRepository stockRepository,
            OrderRepository orderRepository,
            ShippingPricing shippingPricing
    ) {
        this.cartRepository = cartRepository;
        this.stockRepository = stockRepository;
        this.orderRepository = orderRepository;
        this.shippingPricing = shippingPricing;
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
        if (cart == null) {
            throw new IllegalArgumentException("cart required");
        }
        if (shipping == null) {
            throw new IllegalArgumentException("shipping required");
        }
        if (billing == null) {
            throw new IllegalArgumentException("billing required");
        }
        if (carrier == null) {
            throw new IllegalArgumentException("carrier required");
        }
        if (contactEmail == null || contactEmail.isBlank()) {
            throw new IllegalArgumentException("contactEmail required");
        }
        if (cart.getId() <= 0) {
            throw new IllegalArgumentException("Persisted cart required");
        }

        Cart lockedCart = cartRepository.findByIdForUpdate(cart.getId())
                .orElseThrow(() -> new IllegalStateException("Cart not found"));

        if (lockedCart.getStatus() != org.example.amaizon.model.enums.CartStatus.ACTIVE) {
            throw new IllegalStateException("Cart already checked out");
        }

        if (lockedCart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot checkout an empty cart");
        }

        if (userOrNull != null && lockedCart.getUser() == null) {
            lockedCart.assignToUser(userOrNull);
        }

        if (userOrNull != null
                && lockedCart.getUser() != null
                && lockedCart.getUser().getId() != userOrNull.getId()) {
            throw new IllegalStateException("Cart belongs to another user");
        }

        CustomerType type = billingCustomerType == null
                ? CustomerType.INDIVIDUAL
                : billingCustomerType;

        for (CartItem item : lockedCart.getItems()) {
            ProductCountry productCountry = item.getProductCountry();

            if (productCountry == null
                    || productCountry.getCountry() == null
                    || productCountry.getCountry().getId() != lockedCart.getCountry().getId()) {
                throw new IllegalStateException("Product belongs to another market");
            }

            if (item.getQuantity() <= 0) {
                throw new IllegalStateException("Invalid cart quantity");
            }

            ProductStock stock = stockRepository
                    .findByProductCountryForUpdate(productCountry)
                    .orElseThrow(() -> new IllegalStateException("Product stock unavailable"));

            stock.reserve(item.getQuantity());
        }

        Order order = Order.create(
                lockedCart.getUser(),
                lockedCart.getCountry(),
                lockedCart.getCurrency(),
                contactEmail,
                shipping,
                billing,
                type,
                billingCompanyName,
                billingTaxId
        );

        for (CartItem item : lockedCart.getItems()) {
            ProductCountry pc = item.getProductCountry();

            if (pc.getPrice() == null || pc.getPrice().getPrice() == null
                    || pc.getPrice().getPrice().getValue() == null) {
                throw new IllegalStateException("Product price unavailable");
            }

            order.addItem(
                    pc.getProduct(),
                    resolveName(pc),
                    resolveSku(pc.getProduct()),
                    pc.getPrice().getPrice().getValue(),
                    item.getQuantity()
            );
        }

        order.setShippingCost(shippingPricing.price(lockedCart.getCountry().getCode().getValue(), carrier));
        order.createShipment(carrier, "PENDING");

        lockedCart.checkout();
        cartRepository.save(lockedCart);

        return orderRepository.save(order);
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
