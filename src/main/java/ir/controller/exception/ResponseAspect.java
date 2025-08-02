package ir.controller.exception;

import ir.controller.exception.ExceptionWrapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;

import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@Slf4j
public class ResponseAspect {

//    private static final Logger log = LoggerFactory.getLogger(ResponseAspect.class);

//    @Around("execution(* ir.controller.api.UserApi.*(..))")
//    public Object formatResponse(ProceedingJoinPoint joinPoint) throws Throwable {
//        Object result = joinPoint.proceed();
//        Map<String, Object> response = new HashMap<>();
//
//        try{
//            response.put("message", "Operation Successful : " + joinPoint.getSignature().getName());
//            response.put("data", result);
//
//            System.out.println(response);
//
//            return ResponseEntity.ok().body(response);
//        } catch (Exception e){
//            response.put("message", "Error Occurred : " + e.getMessage());
//            return ResponseEntity.ok().body(response);
//        }
//
//    }


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
            log.error("Web Controller Error: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "خطای داخلی سرور: " + ExceptionWrapper.getMessage(e)));
        }
    }

    @Around("execution(* ir.controller.api..*.*(..))")
    public ResponseEntity<?> apiResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Object result = joinPoint.proceed();
            log.info("API: " + joinPoint.getSignature().getName());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("API Error: " + e.getMessage());
            return ResponseEntity.status(500).body(ExceptionWrapper.getMessage(e));
        }
    }

}


