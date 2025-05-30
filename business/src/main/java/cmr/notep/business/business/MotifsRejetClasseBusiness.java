package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.MotifRejetClasse;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.MotifRejetClasseEntity;
import cmr.notep.ressourcesjpa.repository.MotifRejetClasseRepository;
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
public class MotifsRejetClasseBusiness {
    private final DaoAccessorService daoAccessorService;

    public MotifRejetClasse creerMotifRejetClasse(MotifRejetClasse motifRejetClasse) {
        log.info("Création d'un nouveau motif de rejet de classe: {}", motifRejetClasse.getCode());

        MotifRejetClasseEntity entity = dozerMapperBean.map(motifRejetClasse, MotifRejetClasseEntity.class);
        entity.setDateCreation(LocalDateTime.now());

        MotifRejetClasseEntity savedEntity = daoAccessorService.getRepository(MotifRejetClasseRepository.class).save(entity);
        return dozerMapperBean.map(savedEntity, MotifRejetClasse.class);
    }

    public List<MotifRejetClasse> obtenirTousMotifsRejetClasse() {
        return daoAccessorService.getRepository(MotifRejetClasseRepository.class).findAll()
                .stream()
                .map(entity -> dozerMapperBean.map(entity, MotifRejetClasse.class))
                .collect(Collectors.toList());
    }

    public void supprimerMotifRejetClasse(String id) {
        log.info("Suppression du motif de rejet de classe avec l'ID: {}", id);

        MotifRejetClasseEntity rejetEntity = daoAccessorService.getRepository(MotifRejetClasseRepository.class).findById(id)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND,
                        "Motif de rejet de classe introuvable avec l'ID: " + id));

        daoAccessorService.getRepository(MotifRejetClasseRepository.class).delete(rejetEntity);
        log.info("Motif de rejet de classe supprimé avec succès: {}", id);
    }

    public MotifRejetClasse obtenirMotifClasseParCode(String code) {
        MotifRejetClasseEntity entity = daoAccessorService.getRepository(MotifRejetClasseRepository.class)
                .findByCode(code)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND,
                        "Motif de rejet de classe introuvable avec le code: " + code));
        return dozerMapperBean.map(entity, MotifRejetClasse.class);
    }
}