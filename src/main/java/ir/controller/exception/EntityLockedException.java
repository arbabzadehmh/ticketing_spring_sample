package ir.controller.exception;

public class EntityLockedException extends RuntimeException{
    public EntityLockedException(){
        super("This record is currently being edited by another user");
    }
}
