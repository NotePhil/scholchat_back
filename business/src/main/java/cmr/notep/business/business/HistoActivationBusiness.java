package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.HistoActivation;
import cmr.notep.modele.EtatClasse;
import cmr.notep.ressourcesjpa.dao.ClassesEntity;
import cmr.notep.ressourcesjpa.dao.HistoActivationEntity;
import cmr.notep.ressourcesjpa.dao.UtilisateursEntity;
import cmr.notep.ressourcesjpa.repository.ClassesRepository;
import cmr.notep.ressourcesjpa.repository.HistoActivationRepository;
import cmr.notep.ressourcesjpa.repository.UtilisateursRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
@Transactional
public class HistoActivationBusiness {
    private final HistoActivationRepository histoActivationRepository;
    private final ClassesRepository classesRepository;
    private final UtilisateursRepository utilisateursRepository;

    public HistoActivation creerEntreeActivation(HistoActivation histoActivation) {
        log.info("Creating new activation entry for class: {}", histoActivation.getClasseId());

        // Validate and load required entities
        ClassesEntity classe = classesRepository.findById(histoActivation.getClasseId())
                .orElseThrow(() -> new SchoolException(
                        SchoolErrorCode.NOT_FOUND,
                        "Class not found with ID: " + histoActivation.getClasseId()
                ));

        UtilisateursEntity utilisateur = utilisateursRepository.findById(histoActivation.getUtilisateurId())
                .orElseThrow(() -> new SchoolException(
                        SchoolErrorCode.NOT_FOUND,
                        "User not found with ID: " + histoActivation.getUtilisateurId()
                ));

        // Check if class is already active
        if (histoActivationRepository.existsByClasseIdAndIsActive(histoActivation.getClasseId(), true)) {
            throw new SchoolException(
                    SchoolErrorCode.INVALID_STATE,
                    "Class is already active"
            );
        }

        // Create and populate the entity
        HistoActivationEntity entity = new HistoActivationEntity();
        entity.setClasse(classe);
        entity.setUtilisateur(utilisateur);
        entity.setDateActivation(LocalDateTime.now());
        entity.setActive(true);
        entity.setEtatClasse(EtatClasse.ACTIF);

        // Update class status
        classe.setEtat(EtatClasse.ACTIF);
        classesRepository.save(classe);

        HistoActivationEntity savedEntity = histoActivationRepository.save(entity);
        log.info("Activation entry created successfully with ID: {}", savedEntity.getId());

        return convertToDto(savedEntity);
    }

    public HistoActivation desactiverEntree(String id, String motif) {
        log.info("Deactivating activation entry with ID: {}", id);

        HistoActivationEntity entity = histoActivationRepository.findById(id)
                .orElseThrow(() -> new SchoolException(
                        SchoolErrorCode.NOT_FOUND,
                        "Activation entry not found with ID: " + id
                ));

        if (!entity.isActive()) {
            throw new SchoolException(
                    SchoolErrorCode.INVALID_STATE,
                    "Activation entry is already inactive"
            );
        }

        // Update activation entry
        entity.setDateDesactivation(LocalDateTime.now());
        entity.setMotifDesactivation(motif);
        entity.setActive(false);
        entity.setEtatClasse(EtatClasse.INACTIF);

        // Update class status
        ClassesEntity classe = entity.getClasse();
        classe.setEtat(EtatClasse.INACTIF);
        classesRepository.save(classe);

        HistoActivationEntity updatedEntity = histoActivationRepository.save(entity);
        log.info("Activation entry deactivated successfully");

        return convertToDto(updatedEntity);
    }

    public List<HistoActivation> obtenirHistoriqueParClasse(String classeId) {
        log.info("Fetching activation history for class: {}", classeId);
        return histoActivationRepository.findByClasseId(classeId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<HistoActivation> obtenirHistoriqueParUtilisateur(String utilisateurId) {
        log.info("Fetching activation history for user: {}", utilisateurId);
        return histoActivationRepository.findByUtilisateurId(utilisateurId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<HistoActivation> obtenirActivationsActives() {
        log.info("Fetching all active activations");
        return histoActivationRepository.findByIsActive(true).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<HistoActivation> obtenirParEtatClasse(EtatClasse etatClasse) {
        log.info("Fetching activations by class state: {}", etatClasse);
        return histoActivationRepository.findByEtatClasse(etatClasse).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public HistoActivation obtenirDerniereActivation(String classeId) {
        log.info("Fetching last activation for class: {}", classeId);
        return histoActivationRepository.findTopByClasseIdOrderByDateActivationDesc(classeId)
                .map(this::convertToDto)
                .orElseThrow(() -> new SchoolException(
                        SchoolErrorCode.NOT_FOUND,
                        "No activation history found for class: " + classeId
                ));
    }

    private HistoActivation convertToDto(HistoActivationEntity entity) {
        return HistoActivation.builder()
                .id(entity.getId())
                .classeId(entity.getClasse().getId())
                .utilisateurId(entity.getUtilisateur().getId())
                .dateActivation(entity.getDateActivation())
                .dateDesactivation(entity.getDateDesactivation())
                .motifDesactivation(entity.getMotifDesactivation())
                .isActive(entity.isActive())
                .etatClasse(entity.getEtatClasse())
                .build();
    }
}