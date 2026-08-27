package com.aldaleel.ticketing.service;

import com.aldaleel.ticketing.entity.Category;
import com.aldaleel.ticketing.entity.Ticket;
import com.aldaleel.ticketing.entity.User;
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

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
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
                .role(User.Role.CUSTOMER)
                .build();

        agent = User.builder()
                .id(agentId)
                .name("Test Agent")
                .email("agent@test.com")
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
                .build();
    }

    @Test
    void createTicket_shouldCreateTicketSuccessfully() {

        when(userRepository.findById(customerId))
                .thenReturn(Optional.of(customer));

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(ticketRepository.save(any(Ticket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Ticket result = ticketService.createTicket(
                "Test Ticket",
                "Test Description",
                Ticket.Priority.HIGH,
                categoryId,
                customerId
        );

        assertNotNull(result);
        assertEquals("Test Ticket", result.getTitle());
        assertEquals("Test Description", result.getDescription());
        assertEquals(Ticket.Priority.HIGH, result.getPriority());
        assertEquals(Ticket.Status.OPEN, result.getStatus());
        assertEquals(customer, result.getCustomer());
        assertEquals(category, result.getCategory());

        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void createTicket_shouldRejectNonCustomer() {

        User agentUser = User.builder()
                .id(agentId)
                .name("Agent User")
                .email("agent2@test.com")
                .role(User.Role.AGENT)
                .build();

        when(userRepository.findById(agentId))
                .thenReturn(Optional.of(agentUser));

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

        assertEquals(
                "Only customers can create tickets",
                exception.getMessage()
        );

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void createTicket_shouldRejectMissingCustomer() {

        when(userRepository.findById(customerId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.createTicket(
                        "Test Ticket",
                        "Test Description",
                        Ticket.Priority.HIGH,
                        categoryId,
                        customerId
                )
        );

        assertEquals(
                "Customer not found",
                exception.getMessage()
        );

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void createTicket_shouldRejectMissingCategory() {

        when(userRepository.findById(customerId))
                .thenReturn(Optional.of(customer));

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.createTicket(
                        "Test Ticket",
                        "Test Description",
                        Ticket.Priority.HIGH,
                        categoryId,
                        customerId
                )
        );

        assertEquals(
                "Category not found",
                exception.getMessage()
        );

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void assignTicket_shouldAssignAgentSuccessfully() {

        when(ticketRepository.findById(ticketId))
                .thenReturn(Optional.of(ticket));

        when(userRepository.findById(agentId))
                .thenReturn(Optional.of(agent));

        when(ticketRepository.save(any(Ticket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Ticket result = ticketService.assignTicket(
                ticketId,
                agentId
        );

        assertEquals(agent, result.getAssignedAgent());
        assertEquals(Ticket.Status.IN_PROGRESS, result.getStatus());

        verify(ticketRepository).save(ticket);
    }

    @Test
    void assignTicket_shouldRejectNonAgent() {

        when(ticketRepository.findById(ticketId))
                .thenReturn(Optional.of(ticket));

        when(userRepository.findById(customerId))
                .thenReturn(Optional.of(customer));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.assignTicket(
                        ticketId,
                        customerId
                )
        );

        assertEquals(
                "User must have AGENT role",
                exception.getMessage()
        );

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void updateStatus_shouldCreateHistorySuccessfully() {

        ticket.setStatus(Ticket.Status.IN_PROGRESS);

        when(ticketRepository.findById(ticketId))
                .thenReturn(Optional.of(ticket));

        when(userRepository.findById(agentId))
                .thenReturn(Optional.of(agent));

        when(ticketRepository.save(any(Ticket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Ticket result = ticketService.updateStatus(
                ticketId,
                Ticket.Status.RESOLVED,
                agentId
        );

        assertEquals(
                Ticket.Status.RESOLVED,
                result.getStatus()
        );

        verify(historyRepository).save(any());

        verify(ticketRepository).save(ticket);
    }

    @Test
    void updateStatus_shouldRejectInvalidTransition() {

        ticket.setStatus(Ticket.Status.OPEN);

        when(ticketRepository.findById(ticketId))
                .thenReturn(Optional.of(ticket));

        when(userRepository.findById(agentId))
                .thenReturn(Optional.of(agent));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.updateStatus(
                        ticketId,
                        Ticket.Status.RESOLVED,
                        agentId
                )
        );

        assertTrue(
                exception.getMessage()
                        .contains("Invalid status transition")
        );

        verify(historyRepository, never()).save(any());
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void updateStatus_shouldRejectClosedTicket() {

        ticket.setStatus(Ticket.Status.CLOSED);

        when(ticketRepository.findById(ticketId))
                .thenReturn(Optional.of(ticket));

        when(userRepository.findById(agentId))
                .thenReturn(Optional.of(agent));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> ticketService.updateStatus(
                        ticketId,
                        Ticket.Status.IN_PROGRESS,
                        agentId
                )
        );

        assertEquals(
                "Closed tickets cannot be modified",
                exception.getMessage()
        );

        verify(historyRepository, never()).save(any());
        verify(ticketRepository, never()).save(any());
    }
}