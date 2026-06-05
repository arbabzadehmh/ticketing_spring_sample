package ir.controller.exception;


import ir.service.impl.AsyncLogService;
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
    private final AsyncLogService asyncLogService;

    public ResponseAspect(ExceptionWrapper exceptionWrapper, AsyncLogService asyncLogService) {
        this.exceptionWrapper = exceptionWrapper;
        this.asyncLogService = asyncLogService;
    }


    @Around("execution(* ir.controller.web..*.*(..))")
    public Object webControllerResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Object result = joinPoint.proceed();
            String infoMessage = "Web Controller: " + joinPoint.getSignature().getName();
            log.info(infoMessage);
//            asyncLogService.saveLog(
//                    "INFO",
//                    joinPoint.getTarget().getClass().getSimpleName(),
//                    infoMessage,
//                    Thread.currentThread().getName()
//            );
            return result;
        } catch (ValidationException e) {
            String errorMessage ="Web Controller Validation Error in " + joinPoint.getSignature().getName() + ": " + e.getErrors();
            log.error(errorMessage);
            asyncLogService.saveLog(
                    "ERROR",
                    joinPoint.getTarget().getClass().getSimpleName(),
                    errorMessage,
                    Thread.currentThread().getName()
            );
            return ResponseEntity.badRequest().body(e.getErrors()); // بازگرداندن خطا به صورت JSON
        } catch (Exception e) {
            Locale locale = LocaleContextHolder.getLocale(); // گرفتن زبان کاربر
            String message = exceptionWrapper.getMessage(e, locale);
            String errorMessage = "Web Controller Error in " + joinPoint.getSignature().getName() + ": " + e.getMessage();
            log.error(errorMessage);
            asyncLogService.saveLog(
                    "ERROR",
                    joinPoint.getTarget().getClass().getSimpleName(),
                    errorMessage,
                    Thread.currentThread().getName()
            );
            return ResponseEntity.status(500).body(Map.of("error", message));
        }
    }

    @Around("execution(* ir.controller.api..*.*(..))")
    public ResponseEntity<?> apiResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Object result = joinPoint.proceed();
            String infoMessage = "API: " + joinPoint.getSignature().getName();
            log.info(infoMessage);
//            asyncLogService.saveLog(
//                    "INFO",
//                    joinPoint.getTarget().getClass().getSimpleName(),
//                    infoMessage,
//                    Thread.currentThread().getName()
//            );

            if (result instanceof ResponseEntity) {
                return (ResponseEntity<?>) result; // جلوگیری از دابل‌ریسپانس
            }

            return ResponseEntity.ok(result);

        } catch (ValidationException e) {
            String errorMessage ="API Validation Error in " + joinPoint.getSignature().getName() + ": " + e.getErrors();
            log.error(errorMessage);
            asyncLogService.saveLog(
                    "ERROR",
                    joinPoint.getTarget().getClass().getSimpleName(),
                    errorMessage,
                    Thread.currentThread().getName()
            );
            return ResponseEntity.badRequest().body(e.getErrors()); // بازگرداندن خطا به صورت JSON
        } catch (Exception e) {
            Locale locale = LocaleContextHolder.getLocale(); // گرفتن زبان کاربر
            String message = exceptionWrapper.getMessage(e, locale);
            String errorMessage = "API Error in " + joinPoint.getSignature().getName() + ": " + e.getMessage();
            log.error(errorMessage);
            asyncLogService.saveLog(
                    "ERROR",
                    joinPoint.getTarget().getClass().getSimpleName(),
                    errorMessage,
                    Thread.currentThread().getName()
            );
            return ResponseEntity.status(500).body(Map.of("error", message));
        }
    }

}


