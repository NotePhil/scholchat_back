package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.Etablissement;
import cmr.notep.interfaces.modeles.Utilisateurs;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.EtablissementEntity;
import cmr.notep.ressourcesjpa.dao.UtilisateursEntity;
import cmr.notep.ressourcesjpa.repository.EtablissementRepository;
import cmr.notep.ressourcesjpa.repository.UtilisateursRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j

public class EtablissementBusiness {
    private final DaoAccessorService daoAccessorService;

    public EtablissementBusiness(DaoAccessorService daoAccessorService) {
        this.daoAccessorService = daoAccessorService;
    }

    public Etablissement creerEtablissement(Etablissement etablissement) {
            EtablissementEntity entity = dozerMapperBean.map(etablissement, EtablissementEntity.class);
            
            // Handle gestionnaire if provided
            if (etablissement.getGestionnaire() != null && etablissement.getGestionnaire().getId() != null) {
                UtilisateursEntity gestionnaire = daoAccessorService
                        .getRepository(UtilisateursRepository.class)
                        .findById(etablissement.getGestionnaire().getId())
                        .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Gestionnaire introuvable"));
                entity.setGestionnaire(gestionnaire);
            }
            
            EtablissementEntity saved = daoAccessorService.getRepository(EtablissementRepository.class).save(entity);
            return mapToEtablissement(saved);
    }

    public Etablissement modifierEtablissement(String id, Etablissement etablissementModifie) {
            EtablissementRepository repo = daoAccessorService.getRepository(EtablissementRepository.class);
            EtablissementEntity existing = repo.findById(id)
                    .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Établissement non trouvé avec l'ID: " + id));

            // Update basic fields
            if (etablissementModifie.getNom() != null) {
                existing.setNom(etablissementModifie.getNom());
            }
            if (etablissementModifie.getLocalisation() != null) {
                existing.setLocalisation(etablissementModifie.getLocalisation());
            }
            if (etablissementModifie.getPays() != null) {
                existing.setPays(etablissementModifie.getPays());
            }
            if (etablissementModifie.getEmail() != null) {
                existing.setEmail(etablissementModifie.getEmail());
            }
            if (etablissementModifie.getTelephone() != null) {
                existing.setTelephone(etablissementModifie.getTelephone());
            }
            
            existing.setOptionEnvoiMailVersClasse(etablissementModifie.isOptionEnvoiMailVersClasse());
            existing.setOptionTokenGeneral(etablissementModifie.isOptionTokenGeneral());
            existing.setCodeUnique(etablissementModifie.isCodeUnique());
            
            // Handle gestionnaire update
            if (etablissementModifie.getGestionnaire() != null && etablissementModifie.getGestionnaire().getId() != null) {
                UtilisateursEntity gestionnaire = daoAccessorService
                        .getRepository(UtilisateursRepository.class)
                        .findById(etablissementModifie.getGestionnaire().getId())
                        .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Gestionnaire introuvable"));
                existing.setGestionnaire(gestionnaire);
            }

            EtablissementEntity updated = repo.save(existing);
            log.info("Etablissement modifié avec succès: {}", updated.getId());
            return mapToEtablissement(updated);
    }

    public void supprimerEtablissement(String id) {
            EtablissementRepository repo = daoAccessorService.getRepository(EtablissementRepository.class);
            if (!repo.existsById(id)) {
                throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Établissement non trouvé avec l'ID: " + id);
            }
            repo.deleteById(id);
    }

    public Etablissement obtenirEtablissementParId(String id) {
            EtablissementRepository repo = daoAccessorService.getRepository(EtablissementRepository.class);
            EtablissementEntity entity = repo.findById(id)
                    .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Établissement non trouvé avec l'ID: " + id));
            return mapToEtablissement(entity);
    }

    public List<Etablissement> obtenirTousLesEtablissements() {
            EtablissementRepository repo = daoAccessorService.getRepository(EtablissementRepository.class);
            return repo.findAll()
                    .stream()
                    .map(this::mapToEtablissement)
                    .collect(Collectors.toList());
    }
    
    private Etablissement mapToEtablissement(EtablissementEntity entity) {
        Etablissement etablissement = dozerMapperBean.map(entity, Etablissement.class);
        
        // Map gestionnaire if exists
        if (entity.getGestionnaire() != null) {
            Utilisateurs gestionnaire = new Utilisateurs();
            gestionnaire.setId(entity.getGestionnaire().getId());
            gestionnaire.setNom(entity.getGestionnaire().getNom());
            gestionnaire.setPrenom(entity.getGestionnaire().getPrenom());
            gestionnaire.setEmail(entity.getGestionnaire().getEmail());
            etablissement.setGestionnaire(gestionnaire);
        }
        
        return etablissement;
    }
    
    public List<Etablissement> obtenirEtablissementsParGestionnaire(String gestionnaireId) {
        EtablissementRepository repo = daoAccessorService.getRepository(EtablissementRepository.class);
        return repo.findByGestionnaireId(gestionnaireId)
                .stream()
                .map(this::mapToEtablissement)
                .collect(Collectors.toList());
    }

}
