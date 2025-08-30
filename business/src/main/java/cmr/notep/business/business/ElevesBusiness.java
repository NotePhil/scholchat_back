package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.Eleves;
import cmr.notep.interfaces.modeles.Parents;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.ElevesEntity;
import cmr.notep.ressourcesjpa.repository.ElevesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
public class ElevesBusiness {
    private final DaoAccessorService daoAccessorService;

    public ElevesBusiness(DaoAccessorService daoAccessorService) {
        this.daoAccessorService = daoAccessorService;
    }

    public Eleves avoirEleve(String idEleve) {
        log.info("Fetching eleve with ID: {}", idEleve);
        return dozerMapperBean.map(
                daoAccessorService.getRepository(ElevesRepository.class)
                        .findById(idEleve)
                        .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND,"eleve introuvable avec l'ID: " + idEleve)),
                Eleves.class
        );
    }

    public List<Eleves> avoirToutEleves() {
        return daoAccessorService.getRepository(ElevesRepository.class).findAll()
                .stream()
                .map(entity -> dozerMapperBean.map(entity, Eleves.class))
                .collect(Collectors.toList());
    }

    public Eleves posterEleve(Eleves eleve) {
        return dozerMapperBean.map(
                daoAccessorService.getRepository(ElevesRepository.class)
                        .save(dozerMapperBean.map(eleve, ElevesEntity.class)),
                Eleves.class
        );
    }

    public Eleves modifierElevePartiellement(String idEleve, Eleves partialEleve) {
        log.info("Partial update for eleve with ID: {}", idEleve);

        ElevesRepository repository = daoAccessorService.getRepository(ElevesRepository.class);

        // Find existing eleve
        ElevesEntity existingEntity = repository.findById(idEleve)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Eleve introuvable avec l'ID: " + idEleve));

        // Map to domain model
        Eleves existingEleve = dozerMapperBean.map(existingEntity, Eleves.class);

        // Apply partial updates (only non-null fields)
        if (partialEleve.getNom() != null) {
            existingEleve.setNom(partialEleve.getNom());
        }
        if (partialEleve.getPrenom() != null) {
            existingEleve.setPrenom(partialEleve.getPrenom());
        }
        if (partialEleve.getEmail() != null) {
            existingEleve.setEmail(partialEleve.getEmail().toLowerCase());
        }
        if (partialEleve.getTelephone() != null) {
            existingEleve.setTelephone(partialEleve.getTelephone());
        }
        if (partialEleve.getAdresse() != null) {
            existingEleve.setAdresse(partialEleve.getAdresse());
        }
        if (partialEleve.getEtat() != null) {
            existingEleve.setEtat(partialEleve.getEtat());
        }
        if (partialEleve.getNiveau() != null) {
            existingEleve.setNiveau(partialEleve.getNiveau());
        }

        // Map back to entity and save
        ElevesEntity updatedEntity = dozerMapperBean.map(existingEleve, ElevesEntity.class);
        updatedEntity = repository.save(updatedEntity);

        return dozerMapperBean.map(updatedEntity, Eleves.class);
    }


    public List<Parents> obtenirParents(String eleveId) throws SchoolException {
        ElevesEntity eleve = daoAccessorService.getRepository(ElevesRepository.class)
                .findById(eleveId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Élève introuvable"));

        if (eleve.getParents() == null) {
            return Collections.emptyList();
        }

        return eleve.getParents().stream()
                .map(p -> dozerMapperBean.map(p, Parents.class))
                .collect(Collectors.toList());
    }
}