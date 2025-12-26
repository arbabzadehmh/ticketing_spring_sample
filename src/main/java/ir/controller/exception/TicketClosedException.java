package ir.controller.exception;

public class TicketClosedException extends RuntimeException{
    public TicketClosedException() {
        super("Ticket is CLOSED");
    }
}
