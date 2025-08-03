package ir.controller.exception;

public class DuplicateRoleException extends RuntimeException {
    public DuplicateRoleException() {
        super("Role is already taken");
    }
}
