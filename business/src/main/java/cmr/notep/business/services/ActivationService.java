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

        // Validate token structure and expiration
        jwtUtil.validateToken(activationToken);

        // Extract email directly using dedicated method
        String email = jwtUtil.getEmailFromToken(activationToken);
        log.info("Extracted email from token: {}", email);

        Utilisateurs utilisateur = utilisateursBusiness.avoirUtilisateurParEmail(email);

        // Additional security check: Verify token matches user's stored token
        if (!activationToken.equals(utilisateur.getActivationToken())) {
            throw new SchoolException(
                    SchoolErrorCode.INVALID_TOKEN,
                    "Token does not match user's activation token"
            );
        }

        if (utilisateur.getEtat() != EtatUtilisateur.PENDING) {
            throw new SchoolException(
                    SchoolErrorCode.INVALID_OPERATION,
                    "User must be in PENDING state for activation"
            );
        }

        utilisateur.setEtat(EtatUtilisateur.ACTIVE);
        utilisateur.setActivationToken(null);  // Invalidate used token

        return utilisateursBusiness.mettreUtilisateurAJour(utilisateur);
    }

}