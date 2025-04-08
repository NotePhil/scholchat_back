package cmr.notep.business.business;

import cmr.notep.business.config.JwtConfig;
import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.services.ActivationEmailService;
import cmr.notep.business.services.RoleService;
import cmr.notep.business.services.UserValidationService;
import cmr.notep.business.utils.JwtUtil;
import cmr.notep.interfaces.dto.LoginDto;
import cmr.notep.interfaces.modeles.*;
import cmr.notep.modele.EtatUtilisateur;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthBusiness {

    private final PasswordEncoder passwordEncoder;
    private final UtilisateursBusiness utilisateursBusiness;
    private final RefreshTokenBusiness refreshTokenBusiness;
    private final JwtUtil jwtUtil;
    private final JwtConfig jwtConfig;
    private final ActivationEmailService activationEmailService;
    private final RoleService roleService;
    private final UserValidationService userValidationService;

    /**
     * Register a new user account
     *
     * @param utilisateur User information for registration
     * @return Registration result message
     */
    public String registerUser(Utilisateurs utilisateur) {
        log.info("Processing user registration request for email: {}", utilisateur.getEmail());

        // Vérification de la force du mot de passe
        userValidationService.validatePasswordStrength(utilisateur.getPasseAccess());

        // Recherche de l'utilisateur existant
        Utilisateurs existingUser = utilisateursBusiness.avoirUtilisateurParEmail(utilisateur.getEmail());

        // Mise à jour de l'utilisateur
        existingUser.setPasseAccess(passwordEncoder.encode(utilisateur.getPasseAccess()));
        existingUser.setEtat(EtatUtilisateur.ACTIVE);

        // Enregistrement
        utilisateursBusiness.mettreUtilisateurAJour(existingUser);
        log.info("User registration completed successfully for: {}", utilisateur.getEmail());
        return null;
    }

    /**
     * Authenticate a user and generate tokens
     *
     * @param loginRequest User credentials for login
     * @return Authentication response with tokens
     */
    public AuthResponse loginUser(LoginDto loginRequest) {
        log.info("Processing login request for user: {}", loginRequest.getEmail());

        Utilisateurs existingUser = utilisateursBusiness.avoirUtilisateurParEmail(loginRequest.getEmail());

        // Vérification de l'état d'abord
        if (existingUser.getEtat() != EtatUtilisateur.ACTIVE) {
            log.warn("Login attempt for inactive account: {}", loginRequest.getEmail());
            throw new SchoolException(
                    existingUser.getEtat() == EtatUtilisateur.PENDING ?
                            SchoolErrorCode.INVALID_STATE : SchoolErrorCode.INACTIVE_USER,
                    existingUser.getEtat() == EtatUtilisateur.PENDING ?
                            "User account is still pending activation" : "User account is inactive"
            );
        }

        // Vérification du mot de passe ensuite
        if (!passwordEncoder.matches(loginRequest.getPassword(), existingUser.getPasseAccess())) {
            log.warn("Invalid login attempt for user: {}", loginRequest.getEmail());
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "Invalid email or password");
        }

        // Génération du token avec les rôles
        List<String> roles = roleService.determineUserRoles(existingUser);
        String accessToken = jwtUtil.generateAccessToken(existingUser.getEmail(), roles);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getAccessTokenExpirationMillis())
                .userId(existingUser.getId())
                .userEmail(existingUser.getEmail())
                .username(existingUser.getNom() + " " + existingUser.getPrenom())
                .userType(existingUser.getClass().getSimpleName().toLowerCase())
                .userStatus(existingUser.getEtat())
                .build();
    }



}