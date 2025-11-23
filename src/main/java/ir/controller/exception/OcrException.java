package ir.controller.exception;

public class OcrException extends RuntimeException {
    public OcrException() {
        super("OCR failed");
    }
}
