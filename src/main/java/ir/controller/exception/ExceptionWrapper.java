package ir.controller.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import jakarta.persistence.OptimisticLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

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
        } else if (e instanceof EntityLockedException) {
            return messageSource.getMessage("error.entity.locked", null, locale);
        } else if (e instanceof DuplicateRoleException) {
            return messageSource.getMessage("error.role.duplicate", null, locale);
        } else if (e instanceof DuplicatePermissionException) {
            return messageSource.getMessage("error.permission.duplicate", null, locale);
        } else if (e instanceof DuplicateSectionException) {
            return messageSource.getMessage("error.section.duplicate", null, locale);
        } else if (e instanceof RemovingParentSectionException) {
            return messageSource.getMessage("error.section.removing", null, locale);
        } else if (e instanceof SectionAsOwnParentException) {
            return messageSource.getMessage("error.section.own.parent", null, locale);
        } else if (e instanceof SavingSectionWithNoParent) {
            return messageSource.getMessage("error.section.saving", null, locale);
        } else if (e instanceof DescendantsSectionsAsParent) {
            return messageSource.getMessage("error.section.child.parent", null, locale);
        } else if (e instanceof FileReadException) {
            return messageSource.getMessage("error.file.read", null, locale);
        } else if (e instanceof FileStorageException) {
            return messageSource.getMessage("error.file.storage", null, locale);
        } else if (e instanceof OcrException) {
            return messageSource.getMessage("error.file.storage", null, locale);
        } else if (e instanceof TicketClosedException) {
            return messageSource.getMessage("error.ticket.closed", null, locale);
        } else if (e instanceof TicketExpiredException) {
            return messageSource.getMessage("error.ticket.closed", null, locale);
        } else if (e instanceof TicketIsAlreadyClosedException) {
            return messageSource.getMessage("error.ticket.already.closed", null, locale);
        } else if (e instanceof TicketIsAlreadyScoredException) {
            return messageSource.getMessage("error.ticket.already.scored", null, locale);
        } else if (e instanceof DuplicateBuildingException) {
            return messageSource.getMessage("error.building.duplicate", null, locale);
        } else if (e instanceof AddressEmptyException) {
            return messageSource.getMessage("error.address.empty", null, locale);
        } else if (e instanceof OptimisticLockException ||
                e instanceof ObjectOptimisticLockingFailureException) {
            return messageSource.getMessage("error.concurrent.update", null, locale);
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
