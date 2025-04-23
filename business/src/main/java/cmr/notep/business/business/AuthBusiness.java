package cmr.notep.business.business;

import cmr.notep.business.config.JwtConfig;
import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.services.ActivationEmailService;
import cmr.notep.business.services.PasswordResetEmailService;
import cmr.notep.business.services.RoleService;
import cmr.notep.business.services.UserValidationService;
import cmr.notep.business.utils.JwtUtil;
import cmr.notep.interfaces.dto.LoginDto;
import cmr.notep.interfaces.dto.PasswordResetRequest;
import cmr.notep.interfaces.modeles.*;
import cmr.notep.modele.EtatUtilisateur;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component

@Slf4j
public class AuthBusiness {

    private final PasswordEncoder passwordEncoder;
    private final UtilisateursBusiness utilisateursBusiness;
    private final RefreshTokenBusiness refreshTokenBusiness;
    private final JwtUtil jwtUtil;
    private final JwtConfig jwtConfig;
    private final ActivationEmailService activationEmailService;
    private final PasswordResetEmailService passwordResetEmailService;
    private final UserValidationService userValidationService;
    private final RoleService roleService;

public AuthBusiness(PasswordEncoder passwordEncoder, UtilisateursBusiness utilisateursBusiness, JwtUtil jwtUtil, JwtConfig jwtConfig, ActivationEmailService activationEmailService, RefreshTokenBusiness refreshTokenBusiness, PasswordResetEmailService passwordResetEmailService,RoleService roleService, UserValidationService userValidationService) {
    this.passwordEncoder = passwordEncoder;
    this.utilisateursBusiness = utilisateursBusiness;
    this.jwtUtil = jwtUtil;
    this.jwtConfig = jwtConfig;
    this.activationEmailService = activationEmailService;
    this.refreshTokenBusiness = refreshTokenBusiness;
    this.roleService = roleService;
    this.userValidationService = userValidationService;
    this.passwordResetEmailService = passwordResetEmailService;
}

    /**
     * Register a new user account
     *
     * @param utilisateur User information for registration
     * @return Registration result message
     */
    public void registerUser(Utilisateurs utilisateur) {
        log.info("Processing user registration request for email: {}", utilisateur.getEmail());

        // Only validate password strength here (other validations are in posterUtilisateur)
        userValidationService.validatePasswordStrength(utilisateur.getPasseAccess());

        // Find existing user
        Utilisateurs existingUser = utilisateursBusiness.avoirUtilisateurParEmail(utilisateur.getEmail());

        // Update user password and status
        existingUser.setPasseAccess(passwordEncoder.encode(utilisateur.getPasseAccess()));
        existingUser.setEtat(EtatUtilisateur.ACTIVE);

        // Save updated user
        utilisateursBusiness.mettreUtilisateurAJour(existingUser);
        log.info("User registration completed successfully for: {}", utilisateur.getEmail());
    }

    /**
     * Authenticate a user and generate tokens
     *
     * @param loginRequest User credentials for login
     * @return Authentication response with tokens
     */
    public AuthResponse loginUser(LoginDto loginRequest) {
        log.info("Processing login request for user: {}", loginRequest.getEmail());

        // Retrieve user by email
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



    public Utilisateurs getUtilisateurByEmailWithToken(String email, String token) {
        log.info("Fetching user by email with token validation: {}", email);
        log.debug("Incoming token: {}", token);

        // Get user by email
        Utilisateurs utilisateur = utilisateursBusiness.avoirUtilisateurParEmail(email);
        log.debug("Stored token: {}", utilisateur.getActivationToken());

        // Verify token matches user's token
        if (!token.equals(utilisateur.getActivationToken())) {
            throw new SchoolException(SchoolErrorCode.INVALID_TOKEN,
                    "Token does not match user's token. Received: " + token +
                            " Expected: " + utilisateur.getActivationToken());
        }

        return utilisateur;
    }

    public void requestPasswordReset(String email) {
        log.info("Processing password reset request for email: {}", email);

        // Retrieve user by email
        Utilisateurs user = utilisateursBusiness.avoirUtilisateurParEmail(email);

        // Generate reset token
        String resetToken = jwtUtil.generatePasswordResetToken(user.getEmail());

        // Save token to user entity
        user.setResetPasswordToken(resetToken);
        utilisateursBusiness.mettreUtilisateurAJour(user);

        // Send email
        passwordResetEmailService.sendPasswordResetEmail(user, resetToken);

        log.info("Password reset email sent to: {}", email);
    }
    public void resetPassword(PasswordResetRequest request) {
        // Validate token
        if (!jwtUtil.validatePasswordResetToken(request.getToken())) {
            throw new SchoolException(SchoolErrorCode.INVALID_TOKEN, "Invalid or expired reset token");
        }

        // Get user email from token
        String email = jwtUtil.getEmailFromToken(request.getToken());

        // Get user
        Utilisateurs user = utilisateursBusiness.avoirUtilisateurParEmail(email);

        // Validate passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "Passwords do not match");
        }

        // Update password
        user.setPasseAccess(passwordEncoder.encode(request.getNewPassword()));
        user.setResetPasswordToken(null); // Clear the reset token

        // Save user
        utilisateursBusiness.mettreUtilisateurAJour(user);
        log.info("Password reset successful");
    }
    public Utilisateurs registerUserWithToken(Utilisateurs utilisateur, String token) {
        log.info("Registering user with token validation: {}", utilisateur.getEmail());

        // Validate token first
        if (!jwtUtil.validateToken(token)) {
            throw new SchoolException(SchoolErrorCode.INVALID_TOKEN, "Invalid token");
        }

        // Get existing user
        Utilisateurs existingUser = utilisateursBusiness.avoirUtilisateurParEmail(utilisateur.getEmail());

        // Verify token matches user's token
        if (!token.equals(existingUser.getActivationToken())) {
            throw new SchoolException(SchoolErrorCode.INVALID_TOKEN, "Token does not match user's token");
        }

        // Proceed with registration/update
        registerUser(utilisateur);
        return existingUser;
    }
}