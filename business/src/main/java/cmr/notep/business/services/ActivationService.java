package cmr.notep.business.services;

import cmr.notep.business.business.UtilisateursBusiness;
import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.Utilisateurs;
import cmr.notep.modele.EtatUtilisateur;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivationService {

    private final UtilisateursBusiness utilisateursBusiness;

    public Utilisateurs activerUtilisateur(String activationToken) {
        log.info("Activating user with token: {}", activationToken);

        // Ensure we are correctly fetching the user by activation token
        Utilisateurs utilisateur = utilisateursBusiness.avoirUtilisateurParActivationToken(activationToken);

        // If the user is found, proceed with activation
        if (utilisateur != null) {
            // Check if the user is already active
            if (utilisateur.getEtat() == EtatUtilisateur.ACTIVE) {
                throw new SchoolException(SchoolErrorCode.INVALID_OPERATION, "User is already active.");
            }

            // Update the user's state to ACTIVE
            utilisateur.setEtat(EtatUtilisateur.ACTIVE);

            // Save the updated user
            utilisateursBusiness.mettreUtilisateurAJour(utilisateur);
        } else {
            throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Utilisateur introuvable avec l'ID: " + activationToken);
        }

        return utilisateur;
    }
}