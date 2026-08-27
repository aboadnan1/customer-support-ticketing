package com.aldaleel.ticketing.service;

import com.aldaleel.ticketing.entity.Category;
import com.aldaleel.ticketing.entity.Ticket;
import com.aldaleel.ticketing.entity.TicketStatusHistory;
import com.aldaleel.ticketing.entity.User;
import com.aldaleel.ticketing.repository.CategoryRepository;
import com.aldaleel.ticketing.repository.TicketRepository;
import com.aldaleel.ticketing.repository.TicketStatusHistoryRepository;
import com.aldaleel.ticketing.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TicketStatusHistoryRepository historyRepository;

    public TicketService(
            TicketRepository ticketRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            TicketStatusHistoryRepository historyRepository
    ) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.historyRepository = historyRepository;
    }

    public Ticket createTicket(
            String title,
            String description,
            Ticket.Priority priority,
            UUID categoryId,
            UUID customerId
    ) {

        User customer = userRepository.findById(customerId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Customer not found")
                );

        if (customer.getRole() != User.Role.CUSTOMER) {
            throw new IllegalArgumentException(
                    "Only customers can create tickets"
            );
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Category not found")
                );

        Ticket ticket = Ticket.builder()
                .title(title)
                .description(description)
                .priority(
                        priority != null
                                ? priority
                                : Ticket.Priority.MEDIUM
                )
                .status(Ticket.Status.OPEN)
                .category(category)
                .customer(customer)
                .build();

        return ticketRepository.save(ticket);
    }

    @Transactional(readOnly = true)
    public Ticket getTicketById(UUID id) {

        return ticketRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Ticket not found")
                );
    }

    @Transactional(readOnly = true)
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public Ticket updateTicket(
            UUID id,
            String title,
            String description,
            Ticket.Priority priority
    ) {

        Ticket ticket = getTicketById(id);

        if (title != null && !title.isBlank()) {
            ticket.setTitle(title);
        }

        if (description != null && !description.isBlank()) {
            ticket.setDescription(description);
        }

        if (priority != null) {
            ticket.setPriority(priority);
        }

        return ticketRepository.save(ticket);
    }

    public Ticket assignTicket(
            UUID ticketId,
            UUID agentId
    ) {

        Ticket ticket = getTicketById(ticketId);

        User agent = userRepository.findById(agentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Agent not found")
                );

        if (agent.getRole() != User.Role.AGENT) {
            throw new IllegalArgumentException(
                    "User must have AGENT role"
            );
        }

        ticket.setAssignedAgent(agent);

        /*
         * Assigning an OPEN ticket changes its status to IN_PROGRESS.
         * This status change must also be recorded in the history.
         */
        if (ticket.getStatus() == Ticket.Status.OPEN) {

            Ticket.Status currentStatus = ticket.getStatus();
            Ticket.Status newStatus = Ticket.Status.IN_PROGRESS;

            TicketStatusHistory history = TicketStatusHistory.builder()
                    .ticket(ticket)
                    .fromStatus(currentStatus)
                    .toStatus(newStatus)
                    .changedBy(agent)
                    .build();

            ticket.setStatus(newStatus);

            historyRepository.save(history);
        }

        return ticketRepository.save(ticket);
    }

    public Ticket updateStatus(
            UUID ticketId,
            Ticket.Status newStatus,
            UUID changedById
    ) {

        Ticket ticket = getTicketById(ticketId);

        User changedBy = userRepository.findById(changedById)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );

        Ticket.Status currentStatus = ticket.getStatus();

        if (currentStatus == Ticket.Status.CLOSED) {
            throw new IllegalStateException(
                    "Closed tickets cannot be modified"
            );
        }

        validateStatusTransition(
                currentStatus,
                newStatus
        );

        TicketStatusHistory history = TicketStatusHistory.builder()
                .ticket(ticket)
                .fromStatus(currentStatus)
                .toStatus(newStatus)
                .changedBy(changedBy)
                .build();

        ticket.setStatus(newStatus);

        historyRepository.save(history);

        return ticketRepository.save(ticket);
    }

    private void validateStatusTransition(
            Ticket.Status currentStatus,
            Ticket.Status newStatus
    ) {

        if (currentStatus == newStatus) {
            throw new IllegalArgumentException(
                    "Ticket already has this status"
            );
        }

        boolean valid = switch (currentStatus) {

            case OPEN ->
                    newStatus == Ticket.Status.IN_PROGRESS
                            || newStatus == Ticket.Status.CLOSED;

            case IN_PROGRESS ->
                    newStatus == Ticket.Status.RESOLVED
                            || newStatus == Ticket.Status.OPEN;

            case RESOLVED ->
                    newStatus == Ticket.Status.CLOSED
                            || newStatus == Ticket.Status.IN_PROGRESS;

            case CLOSED -> false;
        };

        if (!valid) {
            throw new IllegalArgumentException(
                    "Invalid status transition from "
                            + currentStatus
                            + " to "
                            + newStatus
            );
        }
    }

    public void deleteTicket(UUID id) {

        Ticket ticket = getTicketById(id);

        ticketRepository.delete(ticket);
    }
}