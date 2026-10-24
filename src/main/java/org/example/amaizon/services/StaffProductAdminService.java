package org.example.amaizon.services;

import lombok.RequiredArgsConstructor;

import org.example.amaizon.model.dtos.CreateGlobalProductForm;
import org.example.amaizon.model.entity.Category;
import org.example.amaizon.model.entity.Product;
import org.example.amaizon.repositories.CategoryRepository;
import org.example.amaizon.repositories.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffProductAdminService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public List<Category> listCategoriesForSelect() {
        return categoryRepository.findAll();
    }

    @Transactional
    public Product createGlobalProduct(CreateGlobalProductForm form) {
        Category category = categoryRepository.findByStableId(form.getCategoryStableId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + form.getCategoryStableId()));

        Product p = Product.createProduct(form.getBrand(), form.getGlobalCode(), form.getImageUrl());
        p.defineCategory(category);

        return productRepository.save(p);
    }
}
