package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.HistoActivation;
import cmr.notep.ressourcesjpa.dao.HistoActivationEntity;
import cmr.notep.ressourcesjpa.repository.HistoActivationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
@RequiredArgsConstructor
public class HistoActivationBusiness {

    private final HistoActivationRepository histoActivationRepository;

    public HistoActivation creerEntreeActivation(HistoActivation histoActivation) {
        log.info("Creating new activation entry for class: {}", histoActivation.getClasseId());

        HistoActivationEntity entity = dozerMapperBean.map(histoActivation, HistoActivationEntity.class);
        entity.setDateActivation(LocalDateTime.now());
        entity.setActive(true);

        HistoActivationEntity savedEntity = histoActivationRepository.save(entity);
        log.info("Activation entry created successfully with ID: {}", savedEntity.getId());

        return dozerMapperBean.map(savedEntity, HistoActivation.class);
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

        entity.setDateDesactivation(LocalDateTime.now());
        entity.setMotifDesactivation(motif);
        entity.setActive(false);

        HistoActivationEntity updatedEntity = histoActivationRepository.save(entity);
        log.info("Activation entry deactivated successfully");

        return dozerMapperBean.map(updatedEntity, HistoActivation.class);
    }

    public List<HistoActivation> obtenirHistoriqueParClasse(String classeId) {
        log.info("Fetching activation history for class: {}", classeId);

        return histoActivationRepository.findByClasseId(classeId).stream()
                .map(entity -> dozerMapperBean.map(entity, HistoActivation.class))
                .collect(Collectors.toList());
    }

    public List<HistoActivation> obtenirHistoriqueParProfesseur(String professeurId) {
        log.info("Fetching activation history for professor: {}", professeurId);

        return histoActivationRepository.findByProfesseurId(professeurId).stream()
                .map(entity -> dozerMapperBean.map(entity, HistoActivation.class))
                .collect(Collectors.toList());
    }

    public List<HistoActivation> obtenirActivationsActives() {
        log.info("Fetching all active activations");

        return histoActivationRepository.findByIsActive(true).stream()
                .map(entity -> dozerMapperBean.map(entity, HistoActivation.class))
                .collect(Collectors.toList());
    }
}