package ir.controller.exception;

public class FileReadException extends RuntimeException {
    public FileReadException() {
        super("Can not read file");
    }
}
