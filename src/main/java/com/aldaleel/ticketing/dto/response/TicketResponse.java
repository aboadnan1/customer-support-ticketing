package com.aldaleel.ticketing.dto.response;

import com.aldaleel.ticketing.entity.Ticket;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TicketResponse(
        UUID id,
        String title,
        String description,
        Ticket.Priority priority,
        Ticket.Status status,

        UUID categoryId,
        String categoryName,

        UUID customerId,
        String customerName,

        UUID assignedAgentId,
        String assignedAgentName,

        LocalDateTime createdAt,
        LocalDateTime updatedAt,

        List<CommentResponse> comments,
        List<StatusHistoryResponse> statusHistory
) {
}