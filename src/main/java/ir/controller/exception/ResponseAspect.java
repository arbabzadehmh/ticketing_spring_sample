package ir.controller.exception;


import ir.service.impl.AsyncLogService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


@Aspect
@Component
@Slf4j
public class ResponseAspect {


    private final AsyncLogService asyncLogService;

    public ResponseAspect(AsyncLogService asyncLogService) {
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

        } catch (Exception e) {
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = joinPoint.getSignature().getName();

            String errorMessage;

            // ---------------- Validation logging ----------------
            if (e instanceof ValidationException ve) {

                errorMessage = "VALIDATION_ERROR in "
                        + methodName
                        + " -> "
                        + ve.getErrors();

                asyncLogService.saveLog(
                        "VALIDATION_ERROR",
                        className,
                        ve.getErrors().toString(),
                        Thread.currentThread().getName()
                );

            } else {

                errorMessage = "API_ERROR in "
                        + methodName
                        + " -> "
                        + e.getMessage();

                asyncLogService.saveLog(
                        "ERROR",
                        className,
                        e.getMessage(),
                        Thread.currentThread().getName()
                );
            }

            log.error(errorMessage);

            throw e;
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

        } catch (Exception e) {
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = joinPoint.getSignature().getName();

            String errorMessage;

            // ---------------- Validation logging ----------------
            if (e instanceof ValidationException ve) {

                errorMessage = "VALIDATION_ERROR in "
                        + methodName
                        + " -> "
                        + ve.getErrors();

                asyncLogService.saveLog(
                        "VALIDATION_ERROR",
                        className,
                        ve.getErrors().toString(),
                        Thread.currentThread().getName()
                );

            } else {

                errorMessage = "API_ERROR in "
                        + methodName
                        + " -> "
                        + e.getMessage();

                asyncLogService.saveLog(
                        "ERROR",
                        className,
                        e.getMessage(),
                        Thread.currentThread().getName()
                );
            }

            log.error(errorMessage);

            throw e;

        }
    }

}


