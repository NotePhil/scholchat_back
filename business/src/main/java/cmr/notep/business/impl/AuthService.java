package cmr.notep.business.impl;

import cmr.notep.business.business.AuthBusiness;
import cmr.notep.interfaces.api.AuthApi;
import cmr.notep.interfaces.dto.ChangePasswordRequest;
import cmr.notep.interfaces.dto.LoginDto;
import cmr.notep.interfaces.dto.PasswordResetRequest;
import cmr.notep.interfaces.dto.PasswordSetupRequest;
import cmr.notep.interfaces.modeles.AuthResponse;
import cmr.notep.interfaces.modeles.Utilisateurs;
import cmr.notep.business.services.ActivationService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class AuthService implements AuthApi {

    private final AuthBusiness authBusiness;
    private final ActivationService activationService;
    private final cmr.notep.business.security.CurrentUserService currentUser;

    /** Session ouverte avec le profil élève : ni changement ni ajout de profil (déconnexion puis reconnexion). */
    public static final String MSG_CHANGEMENT_PROFIL_INTERDIT_ELEVE =
            "Vous êtes connecté avec votre profil élève : le changement de profil n'est pas possible depuis ce "
                    + "profil. Déconnectez-vous puis reconnectez-vous en choisissant le profil souhaité.";

    public AuthService(AuthBusiness authBusiness, ActivationService activationService,
                       cmr.notep.business.security.CurrentUserService currentUser) {
        this.authBusiness = authBusiness;
        this.activationService = activationService;
        this.currentUser = currentUser;
    }

    @Override
    public void registerUser(@NonNull Utilisateurs utilisateur) {
        log.info("Registering new user: {}", utilisateur.getEmail());
        authBusiness.registerUser(utilisateur);
    }

    @Override
    public AuthResponse loginUser(@NonNull LoginDto loginDto) {
        log.info("Logging in user: {}", loginDto.getEmail());
        return authBusiness.loginUser(loginDto);
    }

    /**
     * POST /auth/switch-role (route publique) — deux modes :
     * <ul>
     *   <li>avec mot de passe (web, ReAuthModal) : ré-authentification complète, comme /auth/login ;</li>
     *   <li>sans mot de passe : réservé à l'appelant DÉJÀ authentifié par son jeton d'accès (compte ACTIVE,
     *       voir JwtAuthenticationFilter) ; le profil est changé pour son propre compte uniquement.
     *       Le jeton porte déjà tous les rôles actifs du compte, ce mode ne donne donc aucun droit en plus.</li>
     * </ul>
     * Dans les deux cas le profil demandé doit être un rôle ACTIF du compte (un rôle professeur en
     * attente de validation est refusé avec un message explicite).
     */
    @Override
    public AuthResponse switchRole(@NonNull LoginDto switchRequest) {
        log.info("Switching role for user: {} to {}", switchRequest.getEmail(), switchRequest.getSelectedRole());
        // Session élève (jeton d'accès dont le profil choisi est STUDENT) : changement de profil interdit,
        // avec ou sans mot de passe. L'élève se déconnecte puis se reconnecte en choisissant son profil.
        if (currentUser.sessionEleve()) {
            throw new cmr.notep.business.exceptions.SchoolException(
                    cmr.notep.business.exceptions.enums.SchoolErrorCode.CHANGEMENT_PROFIL_INTERDIT_ELEVE,
                    MSG_CHANGEMENT_PROFIL_INTERDIT_ELEVE);
        }
        if (switchRequest.getPassword() == null || switchRequest.getPassword().isBlank()) {
            org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()
                    || auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
                throw new cmr.notep.business.exceptions.SchoolException(
                        cmr.notep.business.exceptions.enums.SchoolErrorCode.UNAUTHORIZED,
                        "Authentification requise pour changer de profil.");
            }
            String email = auth.getName();
            if (switchRequest.getEmail() != null && !switchRequest.getEmail().isBlank()
                    && !switchRequest.getEmail().trim().equalsIgnoreCase(email)) {
                throw cmr.notep.business.security.CurrentUserService.forbidden(
                        "Vous ne pouvez changer de profil que pour votre propre compte.");
            }
            return authBusiness.switchRoleForAuthenticatedUser(email, switchRequest.getSelectedRole());
        }
        // Re-authenticate and return token for new role
        return authBusiness.loginUser(switchRequest);
    }

    @Override
    public Utilisateurs getUtilisateurByEmailWithToken(String email, String token) {
        log.info("Fetching user by email with token: {}", email);
        return authBusiness.getUtilisateurByEmailWithToken(email, token);
    }

    @Override
    public void requestPasswordReset(String email) {
        log.info("Password reset requested for email: {}", email);
        authBusiness.requestPasswordReset(email);
    }

    @Override
    public void resetPassword(PasswordResetRequest request) {
        log.info("Resetting password with a reset token");
        authBusiness.resetPassword(request);
    }

    @Override
    public Utilisateurs registerUserWithToken(Utilisateurs utilisateur, String token) {
        log.info("Registering user with token: {}", utilisateur.getEmail());
        return authBusiness.registerUserWithToken(utilisateur, token);
    }

    @Override
    public Utilisateurs activerUtilisateur(String activationToken) {
        log.info("Activating user with token: {}", activationToken);
        return activationService.activerUtilisateur(activationToken);
    }
//
//    @Override
//    public String refreshToken(String refreshToken) {
//        log.info("Refreshing token: {}", refreshToken);
//        return authBusiness.refreshAccessToken(refreshToken);
//    }

    @Override
    public void registerPassword(PasswordSetupRequest request) {
        log.info("Setting initial password for: {}", request.getEmail());
        // Les deux clients envoient le jeton d'activation reçu par email dans l'en-tête Authorization.
        String activationToken = null;
        org.springframework.web.context.request.RequestAttributes attrs =
                org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        if (attrs instanceof org.springframework.web.context.request.ServletRequestAttributes sra) {
            String header = sra.getRequest().getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                activationToken = header.substring(7).trim();
            }
        }
        authBusiness.registerPassword(request, activationToken);
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
            throw new cmr.notep.business.exceptions.SchoolException(
                    cmr.notep.business.exceptions.enums.SchoolErrorCode.UNAUTHORIZED, "Authentification requise.");
        }
        String userEmail = auth.getName();
        log.info("Changing password for user: {}", userEmail);
        authBusiness.changePassword(userEmail, request);
    }
}