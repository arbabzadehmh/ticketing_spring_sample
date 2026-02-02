package ir.controller.exception;

public class TicketIsAlreadyScoredException extends RuntimeException {
    public TicketIsAlreadyScoredException() {
        super("Ticket is already scored");
    }
}
