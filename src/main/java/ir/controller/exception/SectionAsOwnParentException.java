package ir.controller.exception;

public class SectionAsOwnParentException extends RuntimeException {
    public SectionAsOwnParentException() {
        super("Department cannot be its own parent");
    }
}
