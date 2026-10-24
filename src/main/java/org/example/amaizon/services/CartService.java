package org.example.amaizon.services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.example.amaizon.model.dtos.CartDto;
import org.example.amaizon.model.dtos.CartItemDto;
import org.example.amaizon.model.dtos.MarketContextDto;
import org.example.amaizon.model.dtos.ProductDto;
import org.example.amaizon.model.entity.*;
import org.example.amaizon.model.entity.Currency;
import org.example.amaizon.model.enums.CartStatus;
import org.example.amaizon.repositories.CartRepository;
import org.example.amaizon.repositories.ProductCountryRepository;
import org.example.amaizon.repositories.ProductStockRepository;
import org.example.amaizon.web.GuestTokenCookie;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.LongToIntFunction;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductCountryRepository productCountryRepository;
    private final ProductStockRepository productStockRepository;

    public CartService(CartRepository cartRepository,
                       ProductCountryRepository productCountryRepository,
                       ProductStockRepository productStockRepository) {
        this.cartRepository = cartRepository;
        this.productCountryRepository = productCountryRepository;
        this.productStockRepository = productStockRepository;
    }

    @Transactional
    public Cart getOrCreateCart(MarketContextDto market,
                                User userOrNull,
                                HttpServletRequest req,
                                HttpServletResponse res) {
        Country country = market.getCountry();

        Currency currency = market.getCurrency();

        if (userOrNull != null) {
            Cart userCart = cartRepository
                    .findActiveUserCartWithItems(userOrNull, country, CartStatus.ACTIVE)
                    .orElse(null);

            UUID guestToken = GuestTokenCookie.get(req);

            Cart guestCart = guestToken == null
                    ? null
                    : cartRepository.findActiveGuestCartWithItems(guestToken, country, CartStatus.ACTIVE)
                    .orElse(null);

            if (userCart == null && guestCart != null) {
                clampWithStock(guestCart);
                guestCart.assignToUser(userOrNull);
                GuestTokenCookie.clear(res);
                return cartRepository.save(guestCart);
            }

            if (userCart != null && guestCart != null) {
                for (CartItem item : guestCart.getItems()) {
                    ProductCountry pc = item.getProductCountry();
                    int qty = item.getQuantity();
                    mergeCartItems(userCart, pc, qty);
                }
                GuestTokenCookie.clear(res);
                cartRepository.delete(guestCart);
                return cartRepository.save(userCart);
            }

            if (userCart != null) {
                return userCart;
            }
            try {
                return cartRepository.save(
                        Cart.createForUser(userOrNull, country, currency)
                );
            } catch (DataIntegrityViolationException ex) {
                return cartRepository
                        .findActiveUserCartWithItems(userOrNull, country, CartStatus.ACTIVE)
                        .orElseThrow(() -> ex);
            }
        }
        UUID guestToken = GuestTokenCookie.getOrCreate(req,res);


        return cartRepository
                .findActiveGuestCartWithItems(guestToken, country, CartStatus.ACTIVE)
                .orElseGet(() -> cartRepository.save
                        (Cart.createForGuest(guestToken, country, currency)));

    }

    private void mergeCartItems(Cart targetCart, ProductCountry pc, int qtyToAdd) {
        if (targetCart == null || pc == null || qtyToAdd <= 0)
            return;


        CartItem existing = targetCart.findItemByProductCountry(pc);

        int available = availableStock(pc);

        int currentQty = existing == null
                ? 0
                : existing.getQuantity();

        int desiredQty = currentQty + qtyToAdd;
        int finalQty = Math.min(desiredQty, available);


        if (existing == null) {
            targetCart.addProduct(pc, finalQty);
        } else {
            targetCart.changeQuantity(pc, finalQty);

        }
    }

    private void clampWithStock(Cart cart) {
        if (cart == null)
            return;

        for (CartItem item : cart.getItems()) {
            ProductCountry pc = item.getProductCountry();
            if (pc == null)
                continue;

            int available = availableStock(pc);
            if (available <= 0) {
                cart.removeProduct(pc);
                continue;
            }
            int qty = Math.max(1, item.getQuantity());
            qty = Math.min(available, qty);
            item.changeQuantity(qty);
        }

    }

    private int availableStock(ProductCountry pc) {
        return productStockRepository
                .findByProductCountry(pc)
                .map(ProductStock::getQuantityAvailable)
                .orElse(0);
    }

    @Transactional
    public Cart addToCart(MarketContextDto market,
                          User userOrNull,
                          HttpServletRequest req,
                          HttpServletResponse res,
                          UUID productCountryStableId,
                          int qty) {

        if (productCountryStableId == null) {
            throw new IllegalArgumentException("productCountryStableId is required");
        }

        Cart cart = getOrCreateCart(market, userOrNull, req, res);

        if (qty <= 0) {
            return cart;
        }

        ProductCountry productCountry = productCountryRepository
                .findEntityByProductCountryStableId(productCountryStableId)
                .orElseThrow(() -> new IllegalArgumentException("Product not available"));

        if (productCountry.getCountry() == null
                || productCountry.getCountry().getId() != market.getCountry().getId()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Product not available in current market"
            );
        }

        long available = availableStock(productCountry);

        if (available <= 0) {
            return cart;
        }

        CartItem existing = cart.findItemByProductCountry(productCountry);

        long currentQty = existing == null
                ? 0L :
                existing.getQuantity();

        long desired = currentQty + qty;
        int finalQty = (int) Math.min(desired, available);

        if (existing == null) {

            cart.addProduct(productCountry, finalQty);
        } else {
            cart.changeQuantity(productCountry, finalQty);
        }

        return cart;
    }


    @Transactional
    public Cart updateQty(MarketContextDto market,
                          User userOrNull,
                          HttpServletRequest req,
                          HttpServletResponse res,
                          UUID productCountryStableId,
                          int qty) {

        if (productCountryStableId == null) {
            throw new IllegalArgumentException("productCountryStableId is required");
        }

        Cart cart = getOrCreateCart(market, userOrNull, req, res);
        ProductCountry productCountry = requireProductCountryFromCart(cart, productCountryStableId);

        long available = availableStock(productCountry);

        if (available <= 0) {
            cart.removeProduct(productCountry);
            return cart;
        }

        int finalQty = Math.max(1, qty);
        finalQty = (int) Math.min(finalQty, available);

        cart.changeQuantity(productCountry, finalQty);

        return cart;
    }

    @Transactional
    public Cart removeItem(MarketContextDto market,
                           User userOrNull,
                           HttpServletRequest req,
                           HttpServletResponse res,
                           UUID productCountryStableId) {

        if (productCountryStableId == null) {
            throw new IllegalArgumentException("productCountryStableId is required");
        }

        Cart cart = getOrCreateCart(market, userOrNull, req, res);
        ProductCountry productCountry = requireProductCountryFromCart(cart, productCountryStableId);

        cart.removeProduct(productCountry);

        return cart;
    }

    @Transactional(readOnly = true)
    public CartDto toDto(Cart cart) {
        if (cart == null) {
            return new CartDto(null, List.of());
        }

        List<CartItemDto> items = cart.getItems().stream()
                .map(this::toItemDto)
                .filter(Objects::nonNull)
                .toList();

        return new CartDto(cart.getStableId(), items);
    }

    private CartItemDto toItemDto(CartItem item) {
        if (item == null || item.getProductCountry() == null) {
            return null;
        }

        ProductCountry pc = item.getProductCountry();

        Product product = pc.getProduct();

        long stock = availableStock(pc);

        ProductDto productDto = new ProductDto(
                product.getStableId(),
                pc.getStableId(),
                product.getBrand().getValue(),
                product.getSku().getValue(),
                product.getImageUrl() == null ? null : product.getImageUrl().getValue(),
                pc.getTranslation().getDisplayName().getValue(),
                pc.getPrice().getPrice().getValue(),
                stock
        );

        return new CartItemDto(productDto, item.getQuantity());
    }


    private ProductCountry requireProductCountryFromCart(Cart cart,
                                                         UUID productCountryStableId) {
        if (cart == null || productCountryStableId == null) {
            throw new IllegalArgumentException("Cart item not found");
        }

        for (CartItem item : cart.getItems()) {
            ProductCountry productCountry = item.getProductCountry();

            if (productCountry != null
                    && productCountryStableId.equals(productCountry.getStableId())) {
                return productCountry;
            }
        }

        throw new IllegalArgumentException("Cart item not found");
    }
}
