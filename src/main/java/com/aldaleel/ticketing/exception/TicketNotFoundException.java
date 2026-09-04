package com.aldaleel.ticketing.exception;

import java.util.UUID;

public class TicketNotFoundException extends ApplicationException {

    public TicketNotFoundException(UUID ticketId) {
        super("Ticket not found: " + ticketId);
    }
}
