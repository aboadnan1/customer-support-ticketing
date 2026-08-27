package com.aldaleel.ticketing.mapper;

import com.aldaleel.ticketing.dto.response.CommentResponse;
import com.aldaleel.ticketing.dto.response.StatusHistoryResponse;
import com.aldaleel.ticketing.dto.response.TicketResponse;
import com.aldaleel.ticketing.entity.Comment;
import com.aldaleel.ticketing.entity.Ticket;
import com.aldaleel.ticketing.entity.TicketStatusHistory;

import java.util.Collections;
import java.util.List;

public class TicketMapper {

    private TicketMapper() {
    }

    public static TicketResponse toResponse(Ticket ticket) {

        List<CommentResponse> comments =
                ticket.getComments() == null
                        ? Collections.emptyList()
                        : ticket.getComments()
                        .stream()
                        .map(TicketMapper::toCommentResponse)
                        .toList();

        List<StatusHistoryResponse> statusHistory =
                ticket.getStatusHistory() == null
                        ? Collections.emptyList()
                        : ticket.getStatusHistory()
                        .stream()
                        .map(TicketMapper::toStatusHistoryResponse)
                        .toList();

        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getPriority(),
                ticket.getStatus(),

                ticket.getCategory() != null
                        ? ticket.getCategory().getId()
                        : null,

                ticket.getCategory() != null
                        ? ticket.getCategory().getName()
                        : null,

                ticket.getCustomer() != null
                        ? ticket.getCustomer().getId()
                        : null,

                ticket.getCustomer() != null
                        ? ticket.getCustomer().getName()
                        : null,

                ticket.getAssignedAgent() != null
                        ? ticket.getAssignedAgent().getId()
                        : null,

                ticket.getAssignedAgent() != null
                        ? ticket.getAssignedAgent().getName()
                        : null,

                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),

                comments,
                statusHistory
        );
    }

    private static CommentResponse toCommentResponse(
            Comment comment
    ) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),

                comment.getAuthor() != null
                        ? comment.getAuthor().getId()
                        : null,

                comment.getAuthor() != null
                        ? comment.getAuthor().getName()
                        : null,

                comment.getCreatedAt()
        );
    }

    private static StatusHistoryResponse toStatusHistoryResponse(
            TicketStatusHistory history
    ) {
        return new StatusHistoryResponse(
                history.getId(),
                history.getFromStatus(),
                history.getToStatus(),

                history.getChangedBy() != null
                        ? history.getChangedBy().getId()
                        : null,

                history.getChangedBy() != null
                        ? history.getChangedBy().getName()
                        : null,

                history.getChangedAt()
        );
    }
}