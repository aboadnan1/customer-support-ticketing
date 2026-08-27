package com.aldaleel.ticketing.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateTicketRequest(

        @Size(max = 200, message = "Title must not exceed 200 characters")
        String title,

        String description,

        String priority
) {
}