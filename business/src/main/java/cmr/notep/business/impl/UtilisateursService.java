package cmr.notep.business.impl;

import cmr.notep.business.business.UtilisateursBusiness;
import cmr.notep.interfaces.api.UtilisateursApi;
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
    public Utilisateurs avoirUtilisateur(@NonNull String idUtilisateur) {
        log.info("Récupération de l'utilisateur avec ID: {}", idUtilisateur);
            return utilisateursBusiness.avoirUtilisateur(idUtilisateur);
    }

    @Override
    public List<Utilisateurs> avoirToutUtilisateurs() {
        log.info("Récupération de tous les utilisateurs");
        Utilisateurs utilisateur = Utilisateurs.builder()
                .nom("nom")
                .prenom("prenom")
                .email("email")
                .passeAccess("passeAccess")
                .telephone("telephone")
                .adresse("adresse")
                .build();
        utilisateur = posterUtilisateur(utilisateur);
        log.info("Utilisateur: {}", utilisateur);
        return utilisateursBusiness.avoirToutUtilisateurs();
    }

    @Override
    public Utilisateurs posterUtilisateur(@NonNull Utilisateurs utilisateur) {
        log.info("Création d'un nouvel utilisateur");
        return utilisateursBusiness.posterUtilisateur(utilisateur);
    }
}
