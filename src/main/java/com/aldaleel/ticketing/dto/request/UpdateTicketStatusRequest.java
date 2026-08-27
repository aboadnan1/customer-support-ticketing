package com.aldaleel.ticketing.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateTicketStatusRequest(

        @NotBlank(message = "Status is required")
        String status
) {
}