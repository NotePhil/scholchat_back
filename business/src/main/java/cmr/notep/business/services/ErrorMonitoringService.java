package cmr.notep.business.services;

import cmr.notep.business.exceptions.SchoolErrorEmail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ErrorMonitoringService {
    public void logEmailError(SchoolErrorEmail error) {
        // Implémentez ici la logique de journalisation/monitoring
        log.error("Email error occurred: {} - {}", error.getCode(), error.getMessage());
        // Sauvegarde en base de données ou autre système de monitoring
    }
}