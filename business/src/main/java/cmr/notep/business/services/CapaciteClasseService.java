package cmr.notep.business.services;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.ClassesEntity;
import cmr.notep.ressourcesjpa.dao.ContratEntity;
import cmr.notep.ressourcesjpa.repository.AccederRepository;
import cmr.notep.ressourcesjpa.repository.ClassesRepository;
import cmr.notep.ressourcesjpa.repository.ContratRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Restriction « Élèves max » de l'offre d'une classe.
 * <p>L'offre applicable est celle du contrat courant (le plus récent) de la classe, ou — pour une classe
 * rattachée à un établissement — celle du contrat courant de l'établissement (même règle que
 * ContratBusiness#verifierAbonnementActif). {@code elevesMax} null ou &lt;= 0 : pas de limite.</p>
 * <p>Seuls les élèves membres comptent (lignes acceder dont le compte a un profil élève, enfants gérés par
 * un parent compris) ; les parents, professeurs et modérateurs ne comptent pas.</p>
 */
@Service
@Slf4j
public class CapaciteClasseService {

    private final DaoAccessorService daoAccessorService;

    public CapaciteClasseService(DaoAccessorService daoAccessorService) {
        this.daoAccessorService = daoAccessorService;
    }

    /** Nombre maximum d'élèves de l'offre courante de la classe, ou null si illimité / sans contrat. */
    public Integer elevesMax(ClassesEntity classe) {
        if (classe == null) return null;
        ContratRepository repo = daoAccessorService.getRepository(ContratRepository.class);
        List<ContratEntity> contrats = classe.getEtablissement() != null
                ? repo.findByEtablissementIdOrderByDateCreationDesc(classe.getEtablissement().getId())
                : repo.findByClasseIdOrderByDateCreationDesc(classe.getId());
        if (contrats.isEmpty() || contrats.get(0).getOffre() == null) return null;
        Integer max = contrats.get(0).getOffre().getElevesMax();
        return max != null && max > 0 ? max : null;
    }

    public long nombreEleves(String classeId) {
        return daoAccessorService.getRepository(AccederRepository.class).countElevesByClasseId(classeId);
    }

    /** Vue pour les clients : { elevesMax (null = illimité), nombreEleves, complete }. */
    public Map<String, Object> capacite(String classeId) {
        ClassesEntity classe = daoAccessorService.getRepository(ClassesRepository.class).findById(classeId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe introuvable"));
        Integer max = elevesMax(classe);
        long n = nombreEleves(classeId);
        Map<String, Object> vue = new LinkedHashMap<>();
        vue.put("classeId", classeId);
        vue.put("elevesMax", max);
        vue.put("nombreEleves", n);
        vue.put("complete", max != null && n >= max);
        return vue;
    }

    /**
     * Nouvelle demande / inscription avec le code de la classe : refusée tout de suite si la classe est complète.
     */
    public void verifierPlacePourDemande(ClassesEntity classe) {
        Integer max = elevesMax(classe);
        if (max != null && nombreEleves(classe.getId()) >= max) {
            throw new SchoolException(SchoolErrorCode.CLASSE_COMPLETE,
                    "Cette classe est complète (" + max + " élèves maximum). Contactez l'enseignant.");
        }
    }

    /**
     * Approbation de l'accès d'un élève (ou d'un enfant) : refusée si la classe a déjà atteint le nombre maximum
     * d'élèves de son offre. Sans effet si l'élève est déjà membre de la classe.
     */
    public void verifierPlacePourApprobation(ClassesEntity classe, String eleveId) {
        if (eleveId != null && daoAccessorService.getRepository(AccederRepository.class)
                .existsByUtilisateurIdAndClasseId(eleveId, classe.getId())) {
            return;
        }
        Integer max = elevesMax(classe);
        if (max != null && nombreEleves(classe.getId()) >= max) {
            log.info("Class {} is full ({} students max) — access approval refused", classe.getId(), max);
            throw new SchoolException(SchoolErrorCode.CLASSE_COMPLETE,
                    "La classe « " + classe.getNom() + " » a atteint le nombre maximum d'élèves de son offre (" + max
                            + "). Pour accepter de nouveaux élèves, passez à une offre supérieure.");
        }
    }
}
