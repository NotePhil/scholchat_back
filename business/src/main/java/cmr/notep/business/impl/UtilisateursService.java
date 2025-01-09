package cmr.notep.business.impl;

import cmr.notep.business.business.UtilisateursBusiness;
import cmr.notep.interfaces.api.UtilisateursApi;
import cmr.notep.interfaces.modeles.IUtilisateurs;
import cmr.notep.interfaces.modeles.Utilisateurs;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@Slf4j
public class UtilisateursService implements UtilisateursApi {
    private final UtilisateursBusiness utilisateursBusiness;

    public UtilisateursService(UtilisateursBusiness utilisateursBusiness) {
        this.utilisateursBusiness = utilisateursBusiness;
    }

    @Override
    public Utilisateurs avoirUtilisateur( String idUtilisateur) {
        log.info("Récupération de l'utilisateur avec ID: {}", idUtilisateur);
            return utilisateursBusiness.avoirUtilisateur(idUtilisateur);
    }

    @Override
    public List<Utilisateurs> avoirToutUtilisateurs() {
        log.info("Récupération de tous les utilisateurs");
        return utilisateursBusiness.avoirToutUtilisateurs();
    }

    @Override
    public Utilisateurs posterUtilisateur(@NonNull Utilisateurs utilisateur) {
        log.info("Création d'un nouvel utilisateur");
        return utilisateursBusiness.posterUtilisateur(utilisateur);
    }

    @Override
    public Utilisateurs posterGenericUtilisateur(IUtilisateurs utilisateur) {
        return utilisateursBusiness.posterGenericUtilisateur(utilisateur);
    }
}
