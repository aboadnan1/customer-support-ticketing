package com.aldaleel.ticketing.controller;

import com.aldaleel.ticketing.dto.request.AssignTicketRequest;
import com.aldaleel.ticketing.dto.request.CreateTicketRequest;
import com.aldaleel.ticketing.dto.request.UpdateTicketRequest;
import com.aldaleel.ticketing.dto.request.UpdateTicketStatusRequest;
import com.aldaleel.ticketing.dto.response.TicketResponse;
import com.aldaleel.ticketing.entity.Ticket;
import com.aldaleel.ticketing.mapper.TicketMapper;
import com.aldaleel.ticketing.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(
            @Valid @RequestBody CreateTicketRequest request
    ) {
        Ticket ticket = ticketService.createTicket(
                request.title(),
                request.description(),
                parsePriority(request.priority()),
                request.categoryId(),
                request.customerId()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(TicketMapper.toResponse(ticket));
    }

    @GetMapping
    public ResponseEntity<List<TicketResponse>> getAllTickets() {

        List<TicketResponse> response = ticketService.getAllTickets()
                .stream()
                .map(TicketMapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicketById(
            @PathVariable UUID id
    ) {
        Ticket ticket = ticketService.getTicketById(id);

        return ResponseEntity.ok(
                TicketMapper.toResponse(ticket)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<TicketResponse> updateTicket(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTicketRequest request
    ) {
        Ticket ticket = ticketService.updateTicket(
                id,
                request.title(),
                request.description(),
                parsePriority(request.priority())
        );

        return ResponseEntity.ok(
                TicketMapper.toResponse(ticket)
        );
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<TicketResponse> assignTicket(
            @PathVariable UUID id,
            @Valid @RequestBody AssignTicketRequest request
    ) {
        Ticket ticket = ticketService.assignTicket(
                id,
                request.agentId()
        );

        return ResponseEntity.ok(
                TicketMapper.toResponse(ticket)
        );
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<TicketResponse> updateStatus(
            @PathVariable UUID id,
            @RequestParam UUID changedById,
            @Valid @RequestBody UpdateTicketStatusRequest request
    ) {
        Ticket.Status status = parseStatus(request.status());

        Ticket ticket = ticketService.updateStatus(
                id,
                status,
                changedById
        );

        return ResponseEntity.ok(
                TicketMapper.toResponse(ticket)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(
            @PathVariable UUID id
    ) {
        ticketService.deleteTicket(id);

        return ResponseEntity.noContent().build();
    }

    private Ticket.Priority parsePriority(String priority) {

        if (priority == null || priority.isBlank()) {
            return Ticket.Priority.MEDIUM;
        }

        try {
            return Ticket.Priority.valueOf(
                    priority.trim().toUpperCase()
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid priority. Allowed values: LOW, MEDIUM, HIGH, URGENT"
            );
        }
    }

    private Ticket.Status parseStatus(String status) {

        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException(
                    "Status is required"
            );
        }

        try {
            return Ticket.Status.valueOf(
                    status.trim().toUpperCase()
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid status. Allowed values: OPEN, IN_PROGRESS, RESOLVED, CLOSED"
            );
        }
    }
}