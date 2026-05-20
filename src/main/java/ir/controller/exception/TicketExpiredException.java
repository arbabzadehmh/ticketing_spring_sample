package ir.controller.exception;

public class TicketExpiredException extends RuntimeException{
    public TicketExpiredException() {
        super("Ticket expired");
    }
}
