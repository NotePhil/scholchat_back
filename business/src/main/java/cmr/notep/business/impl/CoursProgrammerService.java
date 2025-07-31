package cmr.notep.business.impl;

import cmr.notep.business.business.CoursProgrammerBusiness;
import cmr.notep.interfaces.api.CoursProgrammerApi;
import cmr.notep.interfaces.modeles.CoursProgrammer;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class CoursProgrammerService implements CoursProgrammerApi {

    private final CoursProgrammerBusiness coursProgrammerBusiness;

    public CoursProgrammerService(CoursProgrammerBusiness coursProgrammerBusiness) {
        this.coursProgrammerBusiness = coursProgrammerBusiness;
    }

    @Override
    public CoursProgrammer programmerCours(@NonNull CoursProgrammer coursProgrammer) {
        log.info("Programming course: {} for date: {}",
                coursProgrammer.getCoursId(), coursProgrammer.getDateCoursPrevue());
        try {
            CoursProgrammer result = coursProgrammerBusiness.programmerCours(coursProgrammer);
            log.info("Course programmed successfully with ID: {}", result.getId());
            return result;
        } catch (Exception e) {
            log.error("Error programming course: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public CoursProgrammer mettreAJourCoursProgramme(@NonNull String id, @NonNull CoursProgrammer coursProgrammer) {
        log.info("Updating scheduled course with ID: {}", id);
        try {
            CoursProgrammer result = coursProgrammerBusiness.mettreAJourCoursProgramme(id, coursProgrammer);
            log.info("Scheduled course updated successfully: {}", id);
            return result;
        } catch (Exception e) {
            log.error("Error updating scheduled course {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void supprimerCoursProgramme(@NonNull String id) {
        log.info("Deleting scheduled course with ID: {}", id);
        try {
            coursProgrammerBusiness.supprimerCoursProgramme(id);
            log.info("Scheduled course deleted successfully: {}", id);
        } catch (Exception e) {
            log.error("Error deleting scheduled course {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public CoursProgrammer obtenirCoursProgrammeParId(@NonNull String id) {
        log.info("Fetching scheduled course with ID: {}", id);
        try {
            CoursProgrammer result = coursProgrammerBusiness.obtenirCoursProgrammeParId(id);
            log.info("Scheduled course fetched successfully: {}", id);
            return result;
        } catch (Exception e) {
            log.error("Error fetching scheduled course {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<CoursProgrammer> obtenirTousLesCoursProgrammes() {
        log.info("Fetching all scheduled courses");
        try {
            List<CoursProgrammer> results = coursProgrammerBusiness.obtenirTousLesCoursProgrammes();
            log.info("Fetched {} scheduled courses", results.size());
            return results;
        } catch (Exception e) {
            log.error("Error fetching all scheduled courses: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<CoursProgrammer> obtenirProgrammationParCours(@NonNull String coursId) {
        log.info("Fetching programming for course: {}", coursId);
        try {
            List<CoursProgrammer> results = coursProgrammerBusiness.obtenirProgrammationParCours(coursId);
            log.info("Found {} programming entries for course: {}", results.size(), coursId);
            return results;
        } catch (Exception e) {
            log.error("Error fetching programming for course {}: {}", coursId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<CoursProgrammer> obtenirProgrammationParClasse(@NonNull String classeId) {
        log.info("Fetching programming for class: {}", classeId);
        try {
            List<CoursProgrammer> results = coursProgrammerBusiness.obtenirProgrammationParClasse(classeId);
            log.info("Found {} programming entries for class: {}", results.size(), classeId);
            return results;
        } catch (Exception e) {
            log.error("Error fetching programming for class {}: {}", classeId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<CoursProgrammer> obtenirProgrammationParParticipant(@NonNull String participantId) {
        log.info("Fetching programming for participant: {}", participantId);
        try {
            List<CoursProgrammer> results = coursProgrammerBusiness.obtenirProgrammationParParticipant(participantId);
            log.info("Found {} programming entries for participant: {}", results.size(), participantId);
            return results;
        } catch (Exception e) {
            log.error("Error fetching programming for participant {}: {}", participantId, e.getMessage(), e);
            throw e;
        }
    }
}