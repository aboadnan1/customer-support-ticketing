package com.aldaleel.ticketing.service;

import com.aldaleel.ticketing.entity.TicketStatusHistory;
import com.aldaleel.ticketing.repository.TicketStatusHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TicketStatusHistoryService {

    private final TicketStatusHistoryRepository historyRepository;

    public TicketStatusHistoryService(
            TicketStatusHistoryRepository historyRepository
    ) {
        this.historyRepository = historyRepository;
    }

    public List<TicketStatusHistory> getHistoryByTicket(UUID ticketId) {
        return historyRepository.findByTicketIdOrderByChangedAtAsc(ticketId);
    }
}