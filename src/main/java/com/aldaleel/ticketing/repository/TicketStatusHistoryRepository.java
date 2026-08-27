package com.aldaleel.ticketing.repository;

import com.aldaleel.ticketing.entity.TicketStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TicketStatusHistoryRepository
        extends JpaRepository<TicketStatusHistory, UUID> {

    List<TicketStatusHistory> findByTicketIdOrderByChangedAtAsc(UUID ticketId);
}