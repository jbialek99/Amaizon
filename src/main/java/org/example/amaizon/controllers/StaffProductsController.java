package org.example.amaizon.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.amaizon.exceptions.imageUrl.ImageUrlValueObjectValidationException;
import org.example.amaizon.exceptions.validation.ProductCountryValidationException;
import org.example.amaizon.model.dtos.CreateGlobalProductForm;
import org.example.amaizon.model.dtos.CreateProductCountryForm;
import org.example.amaizon.model.dtos.MarketContextDto;
import org.example.amaizon.model.entity.Product;
import org.example.amaizon.services.StaffProductAdminService;
import org.example.amaizon.services.StaffProductCountryAdminService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/staff/products")
public class StaffProductsController {

    private final StaffProductAdminService productAdminService;
    private final StaffProductCountryAdminService productCountryAdminService;

    @GetMapping
    @PreAuthorize("hasAnyRole('COUNTRY_ADMIN','HEAD_ADMIN')")
    public String productsMenu() {
        return "staff/products";
    }


    @GetMapping("/add")
    @PreAuthorize("hasRole('HEAD_ADMIN')")
    public String addGlobalForm(Model model) {
        model.addAttribute("form", new CreateGlobalProductForm());
        model.addAttribute("categories", productAdminService.listCategoriesForSelect());
        return "staff/product-add";
    }


    @PostMapping("/add")
    @PreAuthorize("hasRole('HEAD_ADMIN')")
    public String addGlobalSubmit(
            @jakarta.validation.Valid @ModelAttribute("form") CreateGlobalProductForm form,
            BindingResult bindingResult,
            Model model,
            HttpServletRequest request
    ) {
        request.setAttribute(org.example.amaizon.handlers.GlobalExceptionHandler.FORM_VIEW_ATTR, "staff/product-add");
        model.addAttribute("categories", productAdminService.listCategoriesForSelect());

        if (bindingResult.hasErrors()) {
            return "staff/product-add";
        }

        try {
            productAdminService.createGlobalProduct(form);
            return "redirect:/staff/products?createdProduct=1";
        } catch (ImageUrlValueObjectValidationException ex) {
            String key = org.example.amaizon.model.validators.validation.ProductValidation
                    .IMAGE_URL_KEYS.resolve(ex.getReason());
            bindingResult.rejectValue("imageUrl", key, "{" + key + "}");
            return "staff/product-add";
        }
    }


    @GetMapping("/add-country")
    @PreAuthorize("hasAnyRole('COUNTRY_ADMIN','HEAD_ADMIN')")
    public String addCountrySelectBase(Model model,
                                       @ModelAttribute("market") MarketContextDto market) {
        model.addAttribute("market", market);

        model.addAttribute("products", productCountryAdminService.listAllGlobalProducts(market));

        model.addAttribute("alreadyAssigned",
                productCountryAdminService.findAlreadyAssignedProductStableIds(market));

        return "staff/product-country-select";
    }

    @GetMapping("/add-country/{productStableId}")
    @PreAuthorize("hasAnyRole('COUNTRY_ADMIN','HEAD_ADMIN')")
    public String addCountryForm(@PathVariable UUID productStableId,
                                 @ModelAttribute("market") MarketContextDto market,
                                 Model model) {
        model.addAttribute("market", market);

        Product product = productCountryAdminService.getGlobalProduct(productStableId, market);
        model.addAttribute("product", product);

        CreateProductCountryForm form = productCountryAdminService.buildDefaultForm(market);
        model.addAttribute("form", form);

        return "staff/product-country-add";
    }

    @PostMapping("/add-country/{productStableId}")
    @PreAuthorize("hasAnyRole('COUNTRY_ADMIN','HEAD_ADMIN')")
    public String addCountrySubmit(
            @PathVariable UUID productStableId,
            @ModelAttribute("form") @Validated CreateProductCountryForm form,
            @ModelAttribute("market") MarketContextDto market,
            BindingResult br,
            Model model
    ) {
        model.addAttribute("market", market);
        model.addAttribute("product", productCountryAdminService.getGlobalProduct(productStableId, market));

        if (br.hasErrors()) {
            return "staff/product-country-add";
        }

        try {
            productCountryAdminService.createOrUpdateProductCountry(productStableId, market, form);
            return "redirect:/staff/products?createdProductCountry=1";
        } catch (ProductCountryValidationException ex) {
            attachDomainError(ex, br);
            return "staff/product-country-add";
        }
    }

    private void attachDomainError(RuntimeException ex, BindingResult br) {
        String key = ex.getMessage();
        Object[] args = null;

        if (ex instanceof org.example.amaizon.exceptions.KeyedRuntimeException kex) {
            key = kex.getKeyMessage();
            args = kex.getArgs();
        }

        if (args == null) args = new Object[0];
        br.reject(key, args, key);
    }
}
