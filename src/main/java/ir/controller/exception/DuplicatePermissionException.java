package ir.controller.exception;

public class DuplicatePermissionException extends RuntimeException{
    public DuplicatePermissionException(){
        super("Role is already taken");
    }
}
