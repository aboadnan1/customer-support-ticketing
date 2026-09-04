package com.aldaleel.ticketing.service;

import com.aldaleel.ticketing.entity.Category;
import com.aldaleel.ticketing.entity.Ticket;
import com.aldaleel.ticketing.entity.User;
import com.aldaleel.ticketing.exception.InvalidTicketStateTransitionException;
import com.aldaleel.ticketing.exception.UnauthorizedTicketAccessException;
import com.aldaleel.ticketing.repository.CategoryRepository;
import com.aldaleel.ticketing.repository.TicketRepository;
import com.aldaleel.ticketing.repository.TicketStatusHistoryRepository;
import com.aldaleel.ticketing.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TicketStatusHistoryRepository historyRepository;

    @InjectMocks
    private TicketService ticketService;

    private UUID customerId;
    private UUID agentId;
    private UUID categoryId;
    private UUID ticketId;

    private User customer;
    private User agent;
    private Category category;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        agentId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        ticketId = UUID.randomUUID();

        customer = User.builder()
                .id(customerId)
                .name("Test Customer")
                .email("customer@test.com")
                .password("password")
                .role(User.Role.CUSTOMER)
                .build();

        agent = User.builder()
                .id(agentId)
                .name("Test Agent")
                .email("agent@test.com")
                .password("password")
                .role(User.Role.AGENT)
                .build();

        category = Category.builder()
                .id(categoryId)
                .name("Technical Support")
                .description("Technical issues")
                .build();

        ticket = Ticket.builder()
                .id(ticketId)
                .title("Test Ticket")
                .description("Test Description")
                .priority(Ticket.Priority.HIGH)
                .status(Ticket.Status.OPEN)
                .category(category)
                .customer(customer)
                .assignedAgent(agent)
                .build();
    }

    @Test
    void createTicket_shouldCreateTicketSuccessfully() {
        when(userRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ticket result = ticketService.createTicket(
                "Test Ticket",
                "Test Description",
                Ticket.Priority.HIGH,
                categoryId,
                customerId
        );

        assertNotNull(result);
        assertEquals("Test Ticket", result.getTitle());
        assertEquals(Ticket.Status.OPEN, result.getStatus());
        assertEquals(customer, result.getCustomer());
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void createTicket_shouldRejectNonCustomer() {
        when(userRepository.findById(agentId)).thenReturn(Optional.of(agent));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.createTicket(
                        "Test Ticket",
                        "Test Description",
                        Ticket.Priority.HIGH,
                        categoryId,
                        agentId
                )
        );

        assertEquals("Only customers can create tickets", exception.getMessage());
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void assignTicket_shouldMoveOpenTicketToInProgress() {
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(agentId)).thenReturn(Optional.of(agent));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ticket result = ticketService.assignTicket(ticketId, agentId, agentId, User.Role.AGENT);

        assertEquals(agent, result.getAssignedAgent());
        assertEquals(Ticket.Status.IN_PROGRESS, result.getStatus());
        verify(historyRepository).save(any());
    }

    @Test
    void updateStatus_shouldAllowValidTransition() {
        ticket.setStatus(Ticket.Status.IN_PROGRESS);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(agentId)).thenReturn(Optional.of(agent));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ticket result = ticketService.updateStatus(
                ticketId,
                Ticket.Status.RESOLVED,
                agentId,
                agentId,
                User.Role.AGENT
        );

        assertEquals(Ticket.Status.RESOLVED, result.getStatus());
        verify(historyRepository).save(any());
    }

    @Test
    void updateStatus_shouldRejectInvalidTransition() {
        ticket.setStatus(Ticket.Status.OPEN);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(agentId)).thenReturn(Optional.of(agent));

        InvalidTicketStateTransitionException exception = assertThrows(
                InvalidTicketStateTransitionException.class,
                () -> ticketService.updateStatus(
                        ticketId,
                        Ticket.Status.RESOLVED,
                        agentId,
                        agentId,
                        User.Role.AGENT
                )
        );

        assertTrue(exception.getMessage().contains("Invalid ticket status transition"));
        verify(historyRepository, never()).save(any());
    }

    @Test
    void getTicketForUser_shouldRejectUnauthorizedAccess() {
        User secondCustomer = User.builder()
                .id(UUID.randomUUID())
                .name("Other Customer")
                .email("other@test.com")
                .password("secret")
                .role(User.Role.CUSTOMER)
                .build();

        ticket.setCustomer(secondCustomer);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        UnauthorizedTicketAccessException exception = assertThrows(
                UnauthorizedTicketAccessException.class,
                () -> ticketService.getTicketForUser(ticketId, customerId, User.Role.CUSTOMER)
        );

        assertTrue(exception.getMessage().contains("access"));
    }

    @Test
    void getAllTicketsForUser_shouldReturnOnlyAssignedTicketsForAgents() {
        Ticket anotherTicket = Ticket.builder()
                .id(UUID.randomUUID())
                .title("Another")
                .description("Second Description")
                .priority(Ticket.Priority.LOW)
                .status(Ticket.Status.OPEN)
                .category(category)
                .customer(customer)
                .assignedAgent(agent)
                .build();

        when(ticketRepository.findAll()).thenReturn(List.of(ticket, anotherTicket));

        List<Ticket> result = ticketService.getAllTicketsForUser(agentId, User.Role.AGENT);

        assertEquals(2, result.size());
    }
}