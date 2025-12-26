package ir.controller.exception;

public class DuplicateBuildingException extends RuntimeException {
    public DuplicateBuildingException() {
        super("Building is already taken");
    }
}
