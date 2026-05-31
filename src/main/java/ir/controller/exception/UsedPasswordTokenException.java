package ir.controller.exception;

public class UsedPasswordTokenException extends RuntimeException{
    public UsedPasswordTokenException() {
        super("Token already used");
    }
}
