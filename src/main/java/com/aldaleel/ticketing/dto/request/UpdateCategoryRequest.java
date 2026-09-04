package com.aldaleel.ticketing.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateCategoryRequest(
        String name,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description
) {
}
