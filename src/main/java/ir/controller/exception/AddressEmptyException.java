package ir.controller.exception;

public class AddressEmptyException extends RuntimeException{
    public AddressEmptyException() {
        super("Address is empty");
    }
}
