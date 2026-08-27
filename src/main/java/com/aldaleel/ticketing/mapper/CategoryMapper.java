package com.aldaleel.ticketing.mapper;

import com.aldaleel.ticketing.dto.response.CategoryResponse;
import com.aldaleel.ticketing.entity.Category;

public class CategoryMapper {

    private CategoryMapper() {
    }

    public static CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }
}