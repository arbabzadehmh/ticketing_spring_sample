package ir.controller.exception;

public class ExpiredPasswordTokenException extends RuntimeException{
    public ExpiredPasswordTokenException() {
        super("Token expired");
    }
}
