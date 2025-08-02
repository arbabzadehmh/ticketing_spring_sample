package ir.controller.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;

import java.nio.file.AccessDeniedException;
import java.sql.SQLException;

public class ExceptionWrapper {
    public static String getMessage(Exception e) {

        if (e instanceof EntityNotFoundException) {
            return "رکوردی پیدا نشد";
        } else if (e instanceof DataIntegrityViolationException) {
            // چک پیام خطای ORA-00001 در علت اصلی استثنا
            Throwable cause = e.getCause();
            if (cause != null && cause.getCause() != null) {
                String msg = cause.getCause().getMessage();
                if (msg != null && msg.contains("ORA-00001")) {
                    return "نام‌کاربری تکراری است.";
                }
            }
            return "خطا در دیتابیس";
        } else if (e instanceof SQLException) {
            return "خطا در دیتابیس";
        } else if (e instanceof AccessDeniedException) {
            return "اجازه دسترسی ندارید";
        } else if (e instanceof IllegalArgumentException) {
            return "پارامتر نامعتبر";
        } else if (e instanceof NullPointerException) {
            return "خطای داخلی";
        } else {
            return "خطای ناشناخته - تماس با ادمین";
        }
    }
}
