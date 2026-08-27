package com.aldaleel.ticketing.dto.response;

import com.aldaleel.ticketing.entity.Ticket;

import java.time.LocalDateTime;
import java.util.UUID;

public record StatusHistoryResponse(
        UUID id,
        Ticket.Status fromStatus,
        Ticket.Status toStatus,
        UUID changedBy,
        String changedByName,
        LocalDateTime changedAt
) {
}