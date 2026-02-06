package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.Matiere;
import cmr.notep.modele.EtatMatieres;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.MatiereEntity;
import cmr.notep.ressourcesjpa.repository.MatiereRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
public class MatiereBusiness {

    private final DaoAccessorService daoAccessorService;

    public MatiereBusiness(DaoAccessorService daoAccessorService) {
        this.daoAccessorService = daoAccessorService;
    }

    public Matiere creerMatiere(Matiere matiere) {
        // Check if matiere with same name already exists
        if (daoAccessorService.getRepository(MatiereRepository.class).existsByNom(matiere.getNom())) {
            throw new SchoolException(SchoolErrorCode.DUPLICATE_RESOURCE,
                    "Une matière avec ce nom existe déjà: " + matiere.getNom());
        }

        MatiereEntity entity = dozerMapperBean.map(matiere, MatiereEntity.class);
        entity.setId(UUID.randomUUID().toString());
        entity.setDateCreation(LocalDateTime.now());
        entity.setEtat(EtatMatieres.ACTIF);
        MatiereEntity savedEntity = daoAccessorService.getRepository(MatiereRepository.class).save(entity);
        return dozerMapperBean.map(savedEntity, Matiere.class);
    }

    public List<Matiere> obtenirToutesMatieres() {
        return daoAccessorService.getRepository(MatiereRepository.class).findAll()
                .stream()
                .map(m -> dozerMapperBean.map(m, Matiere.class))
                .collect(Collectors.toList());
    }

    public Matiere obtenirMatiereParNom(String nomMatiere) {
        MatiereEntity entity = daoAccessorService.getRepository(MatiereRepository.class)
                .findByNom(nomMatiere)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Matière non trouvée: " + nomMatiere));
        return dozerMapperBean.map(entity, Matiere.class);
    }

    public Matiere modifierMatiere(String id, Matiere matiere) {
        MatiereEntity entity = daoAccessorService.getRepository(MatiereRepository.class)
                .findById(id)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Matière non trouvée avec l'ID: " + id));

        // Check if another matiere with same name exists (excluding current one)
        if (!entity.getNom().equals(matiere.getNom()) && 
            daoAccessorService.getRepository(MatiereRepository.class).existsByNom(matiere.getNom())) {
            throw new SchoolException(SchoolErrorCode.DUPLICATE_RESOURCE,
                    "Une matière avec ce nom existe déjà: " + matiere.getNom());
        }

        entity.setNom(matiere.getNom());
        entity.setDescription(matiere.getDescription());
        MatiereEntity savedEntity = daoAccessorService.getRepository(MatiereRepository.class).save(entity);
        return dozerMapperBean.map(savedEntity, Matiere.class);
    }

    public void supprimerMatiere(String id) {
        MatiereEntity entity = daoAccessorService.getRepository(MatiereRepository.class)
                .findById(id)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Matière non trouvée avec l'ID: " + id));
        
        daoAccessorService.getRepository(MatiereRepository.class).delete(entity);
    }
}