package cmr.notep.business.business;

import cmr.notep.interfaces.modeles.Etablissement;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.EtablissementEntity;
import cmr.notep.ressourcesjpa.repository.EtablissementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
@RequiredArgsConstructor
public class EtablissementBusiness {
    private final DaoAccessorService daoAccessorService;

    public Etablissement creerEtablissement(Etablissement etablissement) {
        try {
            EtablissementEntity entity = dozerMapperBean.map(etablissement, EtablissementEntity.class);
            EtablissementEntity saved = daoAccessorService.getRepository(EtablissementRepository.class).save(entity);
            log.info("Etablissement créé avec succès: {}", saved.getId());
            return dozerMapperBean.map(saved, Etablissement.class);
        } catch (Exception e) {
            log.error("Erreur lors de la création de l'établissement", e);
            throw new RuntimeException("Impossible de créer l'établissement", e);
        }
    }

    public Etablissement modifierEtablissement(String id, Etablissement etablissementModifie) {
        try {
            EtablissementRepository repo = daoAccessorService.getRepository(EtablissementRepository.class);
            EtablissementEntity existing = repo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Etablissement non trouvé"));

            // Update fields
            existing.setNom(etablissementModifie.getNom());

            EtablissementEntity updated = repo.save(existing);
            log.info("Etablissement modifié avec succès: {}", updated.getId());
            return dozerMapperBean.map(updated, Etablissement.class);
        } catch (Exception e) {
            log.error("Erreur lors de la modification de l'établissement", e);
            throw new RuntimeException("Impossible de modifier l'établissement", e);
        }
    }

    public void supprimerEtablissement(String id) {
        try {
            EtablissementRepository repo = daoAccessorService.getRepository(EtablissementRepository.class);
            if (!repo.existsById(id)) {
                log.error("Etablissement non trouvé avec l'ID: {}", id);
                throw new RuntimeException("Etablissement non trouvé");
            }
            repo.deleteById(id);
            log.info("Etablissement supprimé avec succès: {}", id);
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de l'établissement", e);
            throw new RuntimeException("Impossible de supprimer l'établissement", e);
        }
    }

    public Etablissement obtenirEtablissementParId(String id) {
        try {
            EtablissementRepository repo = daoAccessorService.getRepository(EtablissementRepository.class);
            EtablissementEntity entity = repo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Etablissement non trouvé avec l'ID: " + id));
            return dozerMapperBean.map(entity, Etablissement.class);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'établissement", e);
            throw new RuntimeException("Impossible de récupérer l'établissement", e);
        }
    }

    public List<Etablissement> obtenirTousLesEtablissements() {
        try {
            EtablissementRepository repo = daoAccessorService.getRepository(EtablissementRepository.class);
            return repo.findAll()
                    .stream()
                    .map(entity -> dozerMapperBean.map(entity, Etablissement.class))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de tous les établissements", e);
            throw new RuntimeException("Impossible de récupérer les établissements", e);
        }
    }

}
