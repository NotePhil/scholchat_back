package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.MotifRejet;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.MotifRejetEntity;
import cmr.notep.ressourcesjpa.repository.MotifRejetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Slf4j
@Component
public class MotifsRejetBusiness {
    private final DaoAccessorService daoAccessorService;

    public MotifsRejetBusiness(DaoAccessorService daoAccessorService) {
        this.daoAccessorService = daoAccessorService;
    }

    public MotifRejet creerMotifRejet(MotifRejet motifRejet) {
        log.info("Création d'un nouveau motif de rejet: {}", motifRejet.getCode());

        MotifRejetEntity entity = dozerMapperBean.map(motifRejet, MotifRejetEntity.class);
        entity.setId(UUID.randomUUID().toString());
        entity.setDateCreation(LocalDateTime.now());

        MotifRejetEntity savedEntity = daoAccessorService.getRepository(MotifRejetRepository.class).save(entity);
        return dozerMapperBean.map(savedEntity, MotifRejet.class);
    }

    public List<MotifRejet> obtenirTousMotifsRejet() {
        return daoAccessorService.getRepository(MotifRejetRepository.class).findAll()
                .stream()
                .map(entity -> dozerMapperBean.map(entity, MotifRejet.class))
                .collect(Collectors.toList());
    }

    public void supprimerMotifRejet(String id) {
        log.info("Suppression du motif de rejet avec l'ID: {}", id);

        MotifRejetEntity rejetEntity = daoAccessorService.getRepository(MotifRejetRepository.class).findById(id)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Motif de rejet introuvable avec l'ID: " + id));

        daoAccessorService.getRepository(MotifRejetRepository.class).delete(rejetEntity);
        log.info("Motif de rejet supprimé avec succès: {}", id);
    }

    public MotifRejet obtenirMotifParCode(String code) {
        MotifRejetEntity entity = daoAccessorService.getRepository(MotifRejetRepository.class)
                .findByCode(code)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Motif de rejet introuvable avec le code: " + code));
        return dozerMapperBean.map(entity, MotifRejet.class);
    }

    // NEW METHOD: Update rejection motif
    public MotifRejet modifierMotifRejet(String id, MotifRejet motifRejet) {
        log.info("Modification du motif de rejet avec l'ID: {}", id);

        MotifRejetEntity existingEntity = daoAccessorService.getRepository(MotifRejetRepository.class).findById(id)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Motif de rejet introuvable avec l'ID: " + id));

        // Check if code is being changed and if it already exists
        if (!existingEntity.getCode().equals(motifRejet.getCode())) {
            Optional<MotifRejetEntity> existingWithCode = daoAccessorService.getRepository(MotifRejetRepository.class)
                    .findByCode(motifRejet.getCode());
            if (existingWithCode.isPresent() && !existingWithCode.get().getId().equals(id)) {
                throw new SchoolException(SchoolErrorCode.CONFLICT, "Un motif de rejet avec le code " + motifRejet.getCode() + " existe déjà");
            }
        }

        // Update fields
        existingEntity.setCode(motifRejet.getCode());
        existingEntity.setDescriptif(motifRejet.getDescriptif());

        MotifRejetEntity updatedEntity = daoAccessorService.getRepository(MotifRejetRepository.class).save(existingEntity);
        return dozerMapperBean.map(updatedEntity, MotifRejet.class);
    }

    // NEW METHOD: Get motif by ID
    public MotifRejet obtenirMotifParId(String id) {
        MotifRejetEntity entity = daoAccessorService.getRepository(MotifRejetRepository.class).findById(id)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Motif de rejet introuvable avec l'ID: " + id));
        return dozerMapperBean.map(entity, MotifRejet.class);
    }
}