package cmr.notep.business.jobs;

import cmr.notep.business.config.PendingPurgeConfig;
import cmr.notep.ressourcesjpa.repository.UtilisateursRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
@Slf4j
public class PendingUserPurgeJob {
    private final UtilisateursRepository utilisateursRepository;
    private final PendingPurgeConfig pendingPurgeConfig;

    public PendingUserPurgeJob(UtilisateursRepository utilisateursRepository, PendingPurgeConfig pendingPurgeConfig) {
        this.utilisateursRepository = utilisateursRepository;
        this.pendingPurgeConfig = pendingPurgeConfig;
    }

    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2:00 AM
    @Transactional
    public void purgePendingUsers() {
        log.info("Starting purge of pending users...");

        // Calculate the threshold date
        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(pendingPurgeConfig.getPurgeDays());
        Date threshold = Date.from(thresholdDate.atZone(ZoneId.systemDefault()).toInstant());

        // Delete users in PENDING state before the threshold date
        int deletedCount = utilisateursRepository.deleteByEtatAndCreationDateBefore("PENDING", threshold);

        log.info("Purge completed. Total users deleted: {}", deletedCount);
    }
}
