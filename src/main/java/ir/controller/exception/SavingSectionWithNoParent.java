package ir.controller.exception;

public class SavingSectionWithNoParent extends RuntimeException{
    public SavingSectionWithNoParent() {
        super("Saving section with no parent");
    }
}
