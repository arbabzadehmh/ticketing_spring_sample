package ir.controller.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.nio.file.AccessDeniedException;
import java.sql.SQLException;
import java.util.Locale;

@Component
public class ExceptionWrapper {

    private final MessageSource messageSource;

    @Autowired
    public ExceptionWrapper(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(Exception e, Locale locale) {

        if (e instanceof EntityNotFoundException) {
            return messageSource.getMessage("error.entity.notfound", null, locale);
        } else if (e instanceof DataIntegrityViolationException) {
            Throwable cause = e.getCause();
            if (cause != null && cause.getCause() != null) {
                String msg = cause.getCause().getMessage();
                if (msg != null && msg.contains("ORA-00001")) {
                    return messageSource.getMessage("error.username.duplicate", null, locale);
                }
            }
            return messageSource.getMessage("error.database", null, locale);
        } else if (e instanceof DuplicateUsernameException) {
            return messageSource.getMessage("error.username.duplicate", null, locale);
        } else if (e instanceof DuplicateRoleException) {
            return messageSource.getMessage("error.role.duplicate", null, locale);
        } else if (e instanceof DuplicatePermissionException) {
            return messageSource.getMessage("error.permission.duplicate", null, locale);
        } else if (e instanceof SQLException) {
            return messageSource.getMessage("error.database", null, locale);
        } else if (e instanceof AccessDeniedException) {
            return messageSource.getMessage("error.access.denied", null, locale);
        } else if (e instanceof IllegalArgumentException) {
            return messageSource.getMessage("error.invalid.argument", null, locale);
        } else if (e instanceof NullPointerException) {
            return messageSource.getMessage("error.internal", null, locale);
        } else {
            return messageSource.getMessage("error.unknown", null, locale);
        }
    }
}
