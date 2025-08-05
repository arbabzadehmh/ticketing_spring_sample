package ir.controller.exception;

public class DuplicateSectionException extends RuntimeException {
    public DuplicateSectionException() {
        super("Section is already taken");
    }
}
