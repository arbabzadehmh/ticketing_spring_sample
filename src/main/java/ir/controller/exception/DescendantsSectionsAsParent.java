package ir.controller.exception;

public class DescendantsSectionsAsParent extends RuntimeException{
    public DescendantsSectionsAsParent() {
        super("Cannot set a child or descendant as parent");
    }
}
