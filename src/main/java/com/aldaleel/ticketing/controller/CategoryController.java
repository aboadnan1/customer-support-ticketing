package com.aldaleel.ticketing.controller;

import com.aldaleel.ticketing.dto.request.CreateCategoryRequest;
import com.aldaleel.ticketing.dto.request.UpdateCategoryRequest;
import com.aldaleel.ticketing.dto.response.CategoryResponse;
import com.aldaleel.ticketing.entity.Category;
import com.aldaleel.ticketing.mapper.CategoryMapper;
import com.aldaleel.ticketing.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CreateCategoryRequest request
    ) {
        Category category = Category.builder()
                .name(request.name())
                .description(request.description())
                .build();

        Category savedCategory = categoryService.createCategory(category);

        return ResponseEntity.status(HttpStatus.CREATED).body(CategoryMapper.toResponse(savedCategory));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> response = categoryService
                .getAllCategories()
                .stream()
                .map(CategoryMapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @PathVariable UUID id
    ) {
        Category category = categoryService.getCategoryById(id);

        return ResponseEntity.ok(CategoryMapper.toResponse(category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        Category category = Category.builder()
                .name(request.name())
                .description(request.description())
                .build();

        Category updatedCategory = categoryService.updateCategory(id, category);

        return ResponseEntity.ok(CategoryMapper.toResponse(updatedCategory));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable UUID id
    ) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}