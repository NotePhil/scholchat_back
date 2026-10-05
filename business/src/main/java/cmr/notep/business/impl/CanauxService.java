package cmr.notep.business.impl;

import cmr.notep.business.business.CanauxBusiness;
import cmr.notep.interfaces.api.CanauxApi;
import cmr.notep.interfaces.modeles.Canal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class CanauxService implements CanauxApi {
    @org.springframework.beans.factory.annotation.Autowired
    private cmr.notep.business.security.CurrentUserService currentUser;
    @org.springframework.beans.factory.annotation.Autowired
    private cmr.notep.business.security.AccessControlService accessControl;


    private final CanauxBusiness canauxBusiness;

    @Override
    public Canal creerCanal(@NonNull Canal canaux) {
        if (canaux.getClasse() == null || canaux.getClasse().getId() == null) currentUser.requireAdmin(); else accessControl.requireClassTeacher(canaux.getClasse().getId());
        log.info("Tentative de création d'un nouveau canal: {}", canaux);
        Canal nouveauCanal = canauxBusiness.creerCanal(canaux);
        log.info("Canal créé avec succès: {}", nouveauCanal.getId());
        return nouveauCanal;
    }

    @Override
    public Canal modifierCanal(@NonNull String idCanal, @NonNull Canal canalModifie) {
        accessControl.requireCanalManager(idCanal);
        log.info("Tentative de modification du canal avec l'ID: {}", idCanal);
        canalModifie.setId(idCanal);
        Canal canalMAJ = canauxBusiness.modifierCanal(idCanal, canalModifie);
        log.info("Canal modifié avec succès: {}", canalMAJ.getId());
        return canalMAJ;
    }

    @Override
    public void supprimerCanal(@NonNull String idCanal) {
        accessControl.requireCanalManager(idCanal);
        log.info("Tentative de suppression du canal avec l'ID: {}", idCanal);
        canauxBusiness.supprimerCanal(idCanal);
        log.info("Canal supprimé avec succès: {}", idCanal);
    }

    @Override
    public Canal obtenirCanalParId(@NonNull String idCanal) {
        log.info("Récupération du canal avec l'ID: {}", idCanal);
        return canauxBusiness.obtenirCanalParId(idCanal);
    }

    @Override
    public List<Canal> obtenirTousLesCanaux() {
        currentUser.requireAdmin();
        log.info("Récupération de tous les canaux");
        List<Canal> canaux = canauxBusiness.obtenirTousLesCanaux();
        log.info("Récupération de {} canaux", canaux.size());
        return canaux;
    }

    @Override
    public List<Canal> obtenirCanauxParClasse(@NonNull String idClasse) {
        accessControl.requireClassMember(idClasse);
        log.info("Récupération des canaux pour la classe avec l'ID: {}", idClasse);
        List<Canal> canaux = canauxBusiness.obtenirCanauxParClasse(idClasse);
        log.info("Récupération de {} canaux pour la classe {}", canaux.size(), idClasse);
        return canaux;
    }

    @Override
    public List<Canal> obtenirCanauxParProfesseur(@NonNull String idProfesseur) {
        currentUser.requireSelfOrAdmin(idProfesseur);
        log.info("Récupération des canaux pour le professeur avec l'ID: {}", idProfesseur);
        List<Canal> canaux = canauxBusiness.obtenirCanauxParProfesseur(idProfesseur);
        log.info("Récupération de {} canaux pour le professeur {}", canaux.size(), idProfesseur);
        return canaux;
    }
}
