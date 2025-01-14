package cmr.notep.business.services;

import cmr.notep.business.business.UtilisateursBusiness;
import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.utils.JwtUtil;
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
    private final JwtUtil jwtUtil;

    public Utilisateurs activerUtilisateur(String activationToken) {
        log.info("Activating user with token: {}", activationToken);

        // Validate the activation token
        if (!jwtUtil.validateToken(activationToken)) {
            throw new SchoolException(SchoolErrorCode.INVALID_TOKEN, "Token d'activation invalide ou expiré");
        }

        // Extract email from the token
        String email = jwtUtil.extractClaims(activationToken).getSubject();
        log.info("Extracted email from token: {}", email);

        // Fetch the user by email
        Utilisateurs utilisateur = utilisateursBusiness.avoirUtilisateurParEmail(email);

        // Ensure the user's state is PENDING
        if (utilisateur.getEtat() != EtatUtilisateur.PENDING) {
            throw new SchoolException(
                    SchoolErrorCode.INVALID_OPERATION,
                    "L'utilisateur doit être dans l'état PENDING pour être activé."
            );
        }

        // Activate the user
        utilisateur.setEtat(EtatUtilisateur.ACTIVE);
        utilisateur.setActivationToken(null);

        // Save the updated user state
        utilisateursBusiness.mettreUtilisateurAJour(utilisateur);
        log.info("User with email {} has been activated successfully.", email);

        return utilisateur;
    }

}