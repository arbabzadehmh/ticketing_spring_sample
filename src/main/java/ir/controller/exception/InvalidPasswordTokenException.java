package ir.controller.exception;

public class InvalidPasswordTokenException extends RuntimeException{
    public InvalidPasswordTokenException() {
        super("Invalid Password Token");
    }
}
