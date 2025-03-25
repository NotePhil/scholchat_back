package cmr.notep.business.impl;

import cmr.notep.business.business.UtilisateursBusiness;
import cmr.notep.business.services.ActivationService;
import cmr.notep.interfaces.api.UtilisateursApi;
import cmr.notep.interfaces.modeles.IUtilisateurs;
import cmr.notep.interfaces.modeles.Utilisateurs;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@Slf4j
public class UtilisateursService implements UtilisateursApi {
    private final UtilisateursBusiness utilisateursBusiness;
    private final ActivationService activationService;

    public UtilisateursService(UtilisateursBusiness utilisateursBusiness,  ActivationService activationService) {
        this.utilisateursBusiness = utilisateursBusiness;
        this.activationService = activationService;
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
    public Utilisateurs regenererActivationEmail(@RequestParam String email) {
        log.info("Regeneration de l'email d'activation pour: {}", email);
        return utilisateursBusiness.regenererActivationEmail(email);
    }

    @Override
    public Utilisateurs validerProfesseur(String professorId) {
        log.info("Validating professor with ID: {} and comments: {}", professorId);
        return utilisateursBusiness.validerProfesseur(professorId);
    }

    @Override
    public List<Utilisateurs> avoirProfesseursEnAttente() {
        log.info("Fetching all pending professors");
        return utilisateursBusiness.avoirProfesseursEnAttente();
    }
}
