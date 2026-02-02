package ir.controller.exception;

public class TicketIsAlreadyClosedException extends RuntimeException {
    public TicketIsAlreadyClosedException() {
        super("Ticket is already closed");
    }
}
