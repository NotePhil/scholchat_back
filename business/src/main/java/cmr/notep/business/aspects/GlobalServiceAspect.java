package cmr.notep.business.aspects;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class GlobalServiceAspect {

    @Around("execution(* cmr.notep.business.impl.*Service.*(..))")
    public Object handleServiceExceptions(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            // Proceed with the method execution
            return joinPoint.proceed();
        } catch (SchoolException ex) {
            log.error("SchoolException in method: {} with args: {}, Code: {}, Message: {}",
                    joinPoint.getSignature(), joinPoint.getArgs(), ex.getCode(), ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error in method: {} with args: {}, Message: {}",
                    joinPoint.getSignature(), joinPoint.getArgs(), ex.getMessage(), ex);
            throw ex;
        }
    }

}

