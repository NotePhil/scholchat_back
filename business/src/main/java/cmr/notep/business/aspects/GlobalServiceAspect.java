package cmr.notep.business.aspects;

import cmr.notep.business.exceptions.SchoolException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class GlobalServiceAspect {

    @Around("execution(* cmr.notep.business.impl..*(..))")
    public Object handleServiceExceptions(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (SchoolException ex) {
            log.error("SchoolException intercepted in aspect - Code: {}, Message: {}", ex.getCode(), ex.getMessage());
            throw ex; // Rethrow to GlobalExceptionHandler
        } catch (org.springframework.web.server.ResponseStatusException ex) {
            // Allow Spring's ResponseStatusException to propagate normally
            log.error("ResponseStatusException intercepted: {}", ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error intercepted in aspect: {}", ex.getMessage(), ex);
            throw new RuntimeException("Une erreur interne s'est produite.", ex);
        }
    }
}
