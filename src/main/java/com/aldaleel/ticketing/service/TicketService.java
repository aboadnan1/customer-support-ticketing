package com.aldaleel.ticketing.service;

import com.aldaleel.ticketing.entity.Category;
import com.aldaleel.ticketing.entity.Ticket;
import com.aldaleel.ticketing.entity.TicketStatusHistory;
import com.aldaleel.ticketing.entity.User;
import com.aldaleel.ticketing.exception.InvalidTicketStateTransitionException;
import com.aldaleel.ticketing.exception.TicketNotFoundException;
import com.aldaleel.ticketing.exception.UnauthorizedTicketAccessException;
import com.aldaleel.ticketing.exception.UserNotFoundException;
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
                .orElseThrow(() -> new UserNotFoundException(customerId));

        if (customer.getRole() != User.Role.CUSTOMER) {
            throw new IllegalArgumentException("Only customers can create tickets");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        Ticket ticket = Ticket.builder()
                .title(title)
                .description(description)
                .priority(priority != null ? priority : Ticket.Priority.MEDIUM)
                .status(Ticket.Status.OPEN)
                .category(category)
                .customer(customer)
                .build();

        return ticketRepository.save(ticket);
    }

    @Transactional(readOnly = true)
    public Ticket getTicketById(UUID id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Ticket getTicketForUser(UUID ticketId, UUID userId, User.Role userRole) {
        Ticket ticket = getTicketById(ticketId);
        validateTicketAccess(ticket, userId, userRole);
        return ticket;
    }

    @Transactional(readOnly = true)
    public List<Ticket> getAllTicketsForUser(UUID userId, User.Role userRole) {
        if (userRole == User.Role.ADMIN) {
            return ticketRepository.findAll();
        }

        List<Ticket> tickets = ticketRepository.findAll();

        if (userRole == User.Role.CUSTOMER) {
            return tickets.stream()
                    .filter(ticket -> ticket.getCustomer().getId().equals(userId))
                    .toList();
        }

        return tickets.stream()
                .filter(ticket -> ticket.getAssignedAgent() != null
                        && ticket.getAssignedAgent().getId().equals(userId))
                .toList();
    }

    public Ticket updateTicket(
            UUID id,
            String title,
            String description,
            Ticket.Priority priority,
            UUID currentUserId,
            User.Role currentRole
    ) {
        Ticket ticket = getTicketForUser(id, currentUserId, currentRole);

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
            UUID agentId,
            UUID currentUserId,
            User.Role currentRole
    ) {
        if (currentRole != User.Role.AGENT && currentRole != User.Role.ADMIN) {
            throw new UnauthorizedTicketAccessException("Only support agents can assign tickets.");
        }

        Ticket ticket = getTicketById(ticketId);

        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new UserNotFoundException(agentId));

        if (agent.getRole() != User.Role.AGENT) {
            throw new IllegalArgumentException("User must have AGENT role");
        }

        ticket.setAssignedAgent(agent);

        if (ticket.getStatus() == Ticket.Status.OPEN) {
            Ticket.Status currentStatus = ticket.getStatus();
            Ticket.Status newStatus = Ticket.Status.IN_PROGRESS;

            validateStatusTransition(currentStatus, newStatus);

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
            UUID changedById,
            UUID currentUserId,
            User.Role currentRole
    ) {
        if (currentRole == User.Role.CUSTOMER) {
            throw new UnauthorizedTicketAccessException("Customers cannot change ticket status.");
        }

        Ticket ticket = getTicketForUser(ticketId, currentUserId, currentRole);

        User changedBy = userRepository.findById(changedById)
                .orElseThrow(() -> new UserNotFoundException(changedById));

        Ticket.Status currentStatus = ticket.getStatus();
        validateStatusTransition(currentStatus, newStatus);

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

    public void deleteTicket(UUID id, UUID currentUserId, User.Role currentRole) {
        if (currentRole != User.Role.ADMIN) {
            throw new UnauthorizedTicketAccessException("Only administrators can delete tickets.");
        }

        Ticket ticket = getTicketById(id);
        ticketRepository.delete(ticket);
    }

    private void validateTicketAccess(Ticket ticket, UUID userId, User.Role role) {
        if (role == User.Role.ADMIN) {
            return;
        }

        if (role == User.Role.CUSTOMER) {
            if (!ticket.getCustomer().getId().equals(userId)) {
                throw new UnauthorizedTicketAccessException("You do not have access to this ticket.");
            }
            return;
        }

        if (role == User.Role.AGENT) {
            if (ticket.getAssignedAgent() != null && ticket.getAssignedAgent().getId().equals(userId)) {
                return;
            }
            throw new UnauthorizedTicketAccessException("You are not assigned to this ticket.");
        }

        throw new UnauthorizedTicketAccessException("You do not have access to this ticket.");
    }

    private void validateStatusTransition(Ticket.Status currentStatus, Ticket.Status newStatus) {
        if (currentStatus == null || newStatus == null) {
            throw new IllegalArgumentException("Ticket status is required");
        }

        if (currentStatus == newStatus) {
            throw new InvalidTicketStateTransitionException(currentStatus, newStatus);
        }

        if (!ticketStatusIsAllowed(currentStatus, newStatus)) {
            throw new InvalidTicketStateTransitionException(currentStatus, newStatus);
        }
    }

    private boolean ticketStatusIsAllowed(Ticket.Status currentStatus, Ticket.Status newStatus) {
        return switch (currentStatus) {
            case OPEN -> newStatus == Ticket.Status.IN_PROGRESS || newStatus == Ticket.Status.CANCELED;
            case IN_PROGRESS -> newStatus == Ticket.Status.RESOLVED || newStatus == Ticket.Status.CANCELED;
            case RESOLVED -> newStatus == Ticket.Status.CLOSED;
            case CLOSED, CANCELED -> false;
        };
    }
}