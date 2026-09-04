package com.aldaleel.ticketing.exception;

import com.aldaleel.ticketing.entity.Ticket;

public class InvalidTicketStateTransitionException extends ApplicationException {

    public InvalidTicketStateTransitionException(Ticket.Status fromStatus, Ticket.Status toStatus) {
        super("Invalid ticket status transition from " + fromStatus + " to " + toStatus);
    }
}
