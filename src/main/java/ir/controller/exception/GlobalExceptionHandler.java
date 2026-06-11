package ir.controller.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ExceptionWrapper exceptionWrapper;

    public GlobalExceptionHandler(ExceptionWrapper exceptionWrapper) {
        this.exceptionWrapper = exceptionWrapper;
    }

    private ResponseEntity<Map<String, String>> buildErrorResponse(
            Exception ex,
            HttpStatus status) {

        Locale locale = LocaleContextHolder.getLocale();

        String message =
                exceptionWrapper.getMessage(ex, locale);

        return ResponseEntity
                .status(status)
                .body(Map.of("error", message));
    }

    // ---------------- Validation ----------------

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidation(
            ValidationException ex) {

        return ResponseEntity
                .badRequest()
                .body(ex.getErrors());
    }

    // ---------------- Not Found ----------------

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(
            EntityNotFoundException ex) {

        return buildErrorResponse(
                ex,
                HttpStatus.NOT_FOUND
        );
    }

    // ---------------- Conflict ----------------

    @ExceptionHandler({
            DuplicateUsernameException.class,
            DuplicateRoleException.class,
            DuplicatePermissionException.class,
            DuplicateSectionException.class,
            DuplicateBuildingException.class,
            EntityLockedException.class,
            TicketClosedException.class,
            TicketExpiredException.class,
            TicketIsAlreadyClosedException.class,
            TicketIsAlreadyScoredException.class,
            OptimisticLockException.class,
            ObjectOptimisticLockingFailureException.class
    })
    public ResponseEntity<Map<String, String>> handleConflict(
            Exception ex) {

        return buildErrorResponse(
                ex,
                HttpStatus.CONFLICT
        );
    }

    // ---------------- Bad Request ----------------

    @ExceptionHandler({
            AddressEmptyException.class,
            IllegalArgumentException.class,
            SectionAsOwnParentException.class,
            SavingSectionWithNoParent.class,
            DescendantsSectionsAsParent.class,
            RemovingParentSectionException.class,
            InvalidPasswordTokenException.class,
            UsedPasswordTokenException.class,
            ExpiredPasswordTokenException.class
    })
    public ResponseEntity<Map<String, String>> handleBusinessRuleViolation(
            Exception ex) {

        return buildErrorResponse(
                ex,
                HttpStatus.UNPROCESSABLE_ENTITY
        );
    }

    // ---------------- Forbidden ----------------

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(
            AccessDeniedException ex) {

        return buildErrorResponse(
                ex,
                HttpStatus.FORBIDDEN
        );
    }

    // ---------------- Database ----------------

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<Map<String, String>> handleDatabase(
            Exception ex) {

        return buildErrorResponse(
                ex,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    // ---------------- File ----------------

    @ExceptionHandler({
            FileStorageException.class,
            FileReadException.class,
            OcrException.class
    })
    public ResponseEntity<Map<String, String>> handleFile(
            Exception ex) {

        return buildErrorResponse(
                ex,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    // ---------------- Fallback ----------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnknown(
            Exception ex) {

        return buildErrorResponse(
                ex,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    //--------------------------------------------

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrity(
            DataIntegrityViolationException ex) {

        return buildErrorResponse(
                ex,
                HttpStatus.CONFLICT
        );
    }
}

