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
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Slf4j
@Component
@RequiredArgsConstructor
public class MotifsRejetBusiness {
    private final DaoAccessorService daoAccessorService;

    public MotifRejet creerMotifRejet(MotifRejet motifRejet) {
        log.info("Création d'un nouveau motif de rejet: {}", motifRejet.getCode());

        MotifRejetEntity entity = dozerMapperBean.map(motifRejet, MotifRejetEntity.class);
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
}