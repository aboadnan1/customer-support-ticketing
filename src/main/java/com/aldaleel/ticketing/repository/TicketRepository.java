package com.aldaleel.ticketing.repository;

import com.aldaleel.ticketing.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
}