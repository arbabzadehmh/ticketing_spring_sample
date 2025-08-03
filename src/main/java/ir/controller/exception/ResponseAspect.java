package ir.controller.exception;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

@Aspect
@Component
@Slf4j
public class ResponseAspect {

    private final ExceptionWrapper exceptionWrapper;

    public ResponseAspect(ExceptionWrapper exceptionWrapper) {
        this.exceptionWrapper = exceptionWrapper;
    }


    @Around("execution(* ir.controller.web..*.*(..))")
    public Object webControllerResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Object result = joinPoint.proceed();
            log.info("Web Controller: " + joinPoint.getSignature().getName());
            return result;
        } catch (ValidationException e) {
            log.error("Web Controller Validation Error in " + joinPoint.getSignature().getName() + ": " + e.getErrors());
            return ResponseEntity.badRequest().body(e.getErrors()); // بازگرداندن خطا به صورت JSON
        } catch (Exception e) {
            Locale locale = LocaleContextHolder.getLocale(); // گرفتن زبان کاربر
            String message = exceptionWrapper.getMessage(e, locale);
            log.error("Web Controller Error in {}: {}", joinPoint.getSignature().getName(), e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", message));
        }
    }

    @Around("execution(* ir.controller.api..*.*(..))")
    public ResponseEntity<?> apiResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Object result = joinPoint.proceed();
            log.info("API: " + joinPoint.getSignature().getName());

            if (result instanceof ResponseEntity) {
                return (ResponseEntity<?>) result; // جلوگیری از دابل‌ریسپانس
            }

            return ResponseEntity.ok(result);

        } catch (ValidationException e) {
            log.error("API Validation Error in " + joinPoint.getSignature().getName() + ": " + e.getErrors());
            return ResponseEntity.badRequest().body(e.getErrors()); // بازگرداندن خطا به صورت JSON
        } catch (Exception e) {
            Locale locale = LocaleContextHolder.getLocale(); // گرفتن زبان کاربر
            String message = exceptionWrapper.getMessage(e, locale);
            log.error("API Error in {}: {}", joinPoint.getSignature().getName(), e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", message));
        }
    }

}


