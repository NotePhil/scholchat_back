package cmr.notep.business.impl;

import cmr.notep.business.business.ClassesBusiness;
import cmr.notep.business.business.EtablissementBusiness;
import cmr.notep.interfaces.api.EtablissementApi;
import cmr.notep.interfaces.modeles.Etablissement;
import cmr.notep.interfaces.modeles.Utilisateurs;
import cmr.notep.interfaces.modeles.Evenement;
import cmr.notep.business.security.AccessControlService;
import cmr.notep.business.security.CurrentUserService;
import cmr.notep.business.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j

public class EtablissementService implements EtablissementApi {
    private final EtablissementBusiness etablissementBusiness;
    private final ClassesBusiness classesBusiness;
    private final CurrentUserService currentUser;
    private final AccessControlService accessControl;
    private final JwtUtil jwtUtil;

    public EtablissementService(EtablissementBusiness etablissementBusiness, ClassesBusiness classesBusiness,
                                CurrentUserService currentUser, AccessControlService accessControl, JwtUtil jwtUtil) {
        this.etablissementBusiness = etablissementBusiness;
        this.classesBusiness = classesBusiness;
        this.currentUser = currentUser;
        this.accessControl = accessControl;
        this.jwtUtil = jwtUtil;
    }

    /** Le code unique sert à rattacher une classe à l'établissement : seuls son gestionnaire et les admins le voient. */
    private Etablissement masquerCode(Etablissement e) {
        if (e == null || currentUser.isAdmin()) return e;
        String me = currentUser.currentUserIdOpt().orElse(null);
        String gestionnaireId = e.getGestionnaireId() != null ? e.getGestionnaireId()
                : (e.getGestionnaire() != null ? e.getGestionnaire().getId() : null);
        if (me == null || !me.equals(gestionnaireId)) {
            e.setCodeUnique(null);
        }
        return e;
    }

    /**
     * Décision de l'établissement sur une classe en attente : soit via le lien signé de l'email
     * (jeton lié à cette classe et cet établissement), soit par le gestionnaire connecté / un admin.
     * La classe doit appartenir à l'établissement.
     */
    private void verifierDecisionClasse(String classeId, String etablissementId, String token) {
        cmr.notep.ressourcesjpa.dao.ClassesEntity classe = accessControl.getClasse(classeId);
        if (classe.getEtablissement() == null || !etablissementId.equals(classe.getEtablissement().getId())) {
            throw CurrentUserService.forbidden("Cette classe n'est pas rattachée à cet établissement.");
        }
        if (jwtUtil.isValidClassDecisionToken(token, classeId, etablissementId)) {
            return;
        }
        if (!currentUser.isAuthenticated()) {
            throw new cmr.notep.business.exceptions.SchoolException(
                    cmr.notep.business.exceptions.enums.SchoolErrorCode.UNAUTHORIZED,
                    "Lien invalide ou expiré. Connectez-vous en tant que gestionnaire de l'établissement pour traiter cette classe.");
        }
        accessControl.requireEtablissementGestionnaireOrAdmin(etablissementId);
    }



    @Override
    public Etablissement creerEtablissement(Etablissement etablissement) {
        currentUser.requireAdmin();
            log.info("Création d'un nouvel établissement: {}", etablissement.getNom());
            return etablissementBusiness.creerEtablissement(etablissement);
    }

    @Override
    public Etablissement modifierEtablissement(String idEtablissement, Etablissement etablissementModifie) {
        accessControl.requireEtablissementGestionnaireOrAdmin(idEtablissement);
        if (!currentUser.isAdmin()) {
            String me = currentUser.requireUserId();
            boolean changeGestionnaire = etablissementModifie.getGestionnaire() != null
                    && etablissementModifie.getGestionnaire().getId() != null
                    && !me.equals(etablissementModifie.getGestionnaire().getId());
            if (changeGestionnaire) {
                throw CurrentUserService.forbidden("Seul un administrateur peut changer le gestionnaire d'un établissement.");
            }
        }
            log.info("Modification de l'établissement avec ID: {}", idEtablissement);
            return etablissementBusiness.modifierEtablissement(idEtablissement, etablissementModifie);
    }





    @Override
    public void supprimerEtablissement(String idEtablissement) {
        currentUser.requireAdmin();
        log.info("Suppression de l'établissement avec ID: {}", idEtablissement);
            etablissementBusiness.supprimerEtablissement(idEtablissement);
    }

    @Override
    public Etablissement obtenirEtablissementParId(String idEtablissement) {
        log.info("Récupération de l'établissement avec ID: {}", idEtablissement);
            return masquerCode(etablissementBusiness.obtenirEtablissementParId(idEtablissement));
    }

    @Override
    public List<Etablissement> obtenirTousLesEtablissements() {
        log.info("Récupération de tous les établissements");
        List<Etablissement> list = etablissementBusiness.obtenirTousLesEtablissements();
        list.forEach(this::masquerCode);
        return list;
    }
    
    @Override
    public List<Etablissement> obtenirEtablissementsParGestionnaire(String gestionnaireId) {
        log.info("Récupération des établissements gérés par: {}", gestionnaireId);
        List<Etablissement> list = etablissementBusiness.obtenirEtablissementsParGestionnaire(gestionnaireId);
        list.forEach(this::masquerCode);
        return list;
    }
    
    @Override
    public Utilisateurs obtenirGestionnaireEtablissement(String idEtablissement) {
        log.info("Récupération du gestionnaire de l'établissement: {}", idEtablissement);
        return etablissementBusiness.obtenirGestionnaireEtablissement(idEtablissement);
    }

    @Override
    public void approuverClasseParEtablissement(String classeId, String etablissementId, String token) {
        verifierDecisionClasse(classeId, etablissementId, token);
        log.info("Approbation de la classe {} par l'établissement {}", classeId, etablissementId);
        classesBusiness.approuverClasseParEtablissement(classeId, etablissementId);
    }

    @Override
    public void rejeterClasseParEtablissement(String classeId, String etablissementId, String token) {
        verifierDecisionClasse(classeId, etablissementId, token);
        log.info("Rejet de la classe {} par l'établissement {}", classeId, etablissementId);
        classesBusiness.rejeterClasseParEtablissement(classeId, etablissementId);
    }
}
