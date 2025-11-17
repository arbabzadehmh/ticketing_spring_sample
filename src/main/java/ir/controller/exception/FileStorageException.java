package ir.controller.exception;

public class FileStorageException extends RuntimeException {
    public FileStorageException() {
        super("Cannot store file");
    }
}
