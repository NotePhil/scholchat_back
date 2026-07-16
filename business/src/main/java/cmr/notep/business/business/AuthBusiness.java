package cmr.notep.business.business;

import cmr.notep.business.config.JwtConfig;
import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.services.ActivationEmailService;
import cmr.notep.business.services.PasswordDecryptionService;
import cmr.notep.business.services.PasswordResetEmailService;
import cmr.notep.business.services.RoleService;
import cmr.notep.business.services.UserValidationService;
import cmr.notep.business.utils.JwtUtil;
import cmr.notep.interfaces.dto.ChangePasswordRequest;
import cmr.notep.interfaces.dto.LoginDto;
import cmr.notep.interfaces.dto.PasswordResetRequest;
import cmr.notep.interfaces.dto.PasswordSetupRequest;
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
    private final PasswordDecryptionService passwordDecryptionService;
    private final ContratBusiness contratBusiness;


    public AuthBusiness(PasswordEncoder passwordEncoder, UtilisateursBusiness utilisateursBusiness, JwtUtil jwtUtil, JwtConfig jwtConfig, ActivationEmailService activationEmailService, RefreshTokenBusiness refreshTokenBusiness, PasswordResetEmailService passwordResetEmailService,RoleService roleService, UserValidationService userValidationService, PasswordDecryptionService passwordDecryptionService, ContratBusiness contratBusiness) {
        this.passwordEncoder = passwordEncoder;
        this.utilisateursBusiness = utilisateursBusiness;
        this.jwtUtil = jwtUtil;
        this.jwtConfig = jwtConfig;
        this.activationEmailService = activationEmailService;
        this.refreshTokenBusiness = refreshTokenBusiness;
        this.roleService = roleService;
        this.userValidationService = userValidationService;
        this.passwordResetEmailService = passwordResetEmailService;
        this.passwordDecryptionService = passwordDecryptionService;
        this.contratBusiness = contratBusiness;
    }

    /**
     * Register a new user account
     *
     * @param utilisateur User information for registration
     * @return Registration result message
     */
    public Utilisateurs registerUser(Utilisateurs utilisateur) {
        log.info("Processing user registration request for email: {}", utilisateur.getEmail());

        // Vérification de la force du mot de passe
        userValidationService.validatePasswordStrength(utilisateur.getPasseAccess());
        // Check if email already exists
        Utilisateurs existingUser = utilisateursBusiness.avoirUtilisateurParEmail(utilisateur.getEmail());
        if (existingUser == null) {
            log.warn("Email already registered: {}", utilisateur.getEmail());
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "Email already registered");
        }

        // Update user data
        // Encode password before saving
        log.debug("Encoding password for user: {}", utilisateur.getEmail());
        existingUser.setPasseAccess(passwordEncoder.encode(utilisateur.getPasseAccess()));
        existingUser.setEtat(EtatUtilisateur.ACTIVE);

        // Enregistrement
        utilisateursBusiness.mettreUtilisateurAJour(existingUser);
        log.info("User registration completed successfully for: {}", utilisateur.getEmail());
        return null;
    }


    public void registerPassword(PasswordSetupRequest request) {
        log.info("Processing password setup for: {}", request.getEmail());

        // Get user by email
        Utilisateurs user = utilisateursBusiness.avoirUtilisateurParEmail(request.getEmail());
        if (user == null) {
            throw new SchoolException(SchoolErrorCode.NOT_FOUND, "User not found");
        }

        // Validate password strength
        validatePasswordStrength(request.getPasseAccess());

        // Update password and activate account
        user.setPasseAccess(passwordEncoder.encode(request.getPasseAccess()));
        user.setEtat(EtatUtilisateur.ACTIVE);
        user.setActivationToken(null); // Invalidate token now that password is set

        // Save user
        utilisateursBusiness.mettreUtilisateurAJour(user);

        log.info("Password set successfully for: {}", request.getEmail());
    }
    /**
     * Authenticate a user and generate tokens
     *
     * @param loginRequest User credentials for login
     * @return Authentication response with tokens
     */
    public AuthResponse loginUser(LoginDto loginRequest) {
        log.info("Processing login request for user: {}", loginRequest.getEmail());

        // Decrypt the password from client
        String decryptedPassword;
        try {
            decryptedPassword = passwordDecryptionService.decryptPassword(loginRequest.getPassword());
            log.debug("Password decrypted successfully for user: {}", loginRequest.getEmail());
        } catch (Exception e) {
            log.error("Password decryption failed for user: {}", loginRequest.getEmail());
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "Invalid password format");
        }

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

        // Vérification du mot de passe ensuite (using decrypted password)
        if (!passwordEncoder.matches(decryptedPassword, existingUser.getPasseAccess())) {
            log.warn("Invalid login attempt for user: {}", loginRequest.getEmail());
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "Invalid email or password");
        }

        // Get all roles: merge user_roles table + JPA type detection
        List<String> dbRoles = utilisateursBusiness.getUserRoles(existingUser.getId());
        List<String> allDbRoleTypes = utilisateursBusiness.getAllUserRoleTypes(existingUser.getId());
        
        List<String> jpaRoles = roleService.determineUserRoles(existingUser).stream()
                .map(r -> r.replace("ROLE_", ""))
                .filter(r -> !r.equals("USER"))
                // Only add JPA role if it doesn't exist in DB at all (handles legacy data)
                // If it exists in DB but is not in dbRoles, it's inactive (e.g. pending professor)
                .filter(r -> !allDbRoleTypes.contains(r))
                .collect(java.util.stream.Collectors.toList());

        // Merge both sources (no duplicates)
        java.util.Set<String> allRolesSet = new java.util.LinkedHashSet<>(dbRoles);
        allRolesSet.addAll(jpaRoles);
        List<String> availableRoles = new java.util.ArrayList<>(allRolesSet);

        // Sync missing roles to user_roles table for next login
        for (String role : availableRoles) {
            utilisateursBusiness.addRoleToUser(existingUser.getId(), role);
        }

        // Determine selected role (from request or first available)
        String selectedRole = loginRequest.getSelectedRole();
        if (selectedRole == null || selectedRole.isEmpty()) {
            selectedRole = availableRoles.isEmpty() ? "USER" : availableRoles.get(0);
        }

        // Generate token with all roles
        List<String> tokenRoles = availableRoles.stream()
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .collect(java.util.stream.Collectors.toList());
        tokenRoles.add("ROLE_USER");

        // Un utilisateur peut moderer/acceder a plusieurs classes (ou gerer plusieurs etablissements) :
        // on ne bloque la connexion QUE si TOUTES ses classes/etablissements ont une offre expiree
        // (plus aucun acces utile). S'il lui en reste au moins une active, la connexion se fait
        // normalement et sans interruption ; l'avertissement pour l'entite expiree specifique est
        // alors affiche au moment ou l'utilisateur ouvre cette classe/etablissement (voir OffreInfoPanel
        // cote frontend), pas a la connexion. Le vrai verrou d'ecriture (403) reste applique au niveau
        // des actions concernees, voir ContratBusiness.verifierAbonnementActif.
        boolean isAdminUser = availableRoles.stream().anyMatch(r -> r.equalsIgnoreCase("ADMIN"));
        ContratBusiness.AccesUtilisateurInfo acces = isAdminUser
                ? new ContratBusiness.AccesUtilisateurInfo(java.util.Collections.emptyList(), true)
                : contratBusiness.resoudreAccesUtilisateur(existingUser.getId());

        if (!acces.isHasActiveEntity() && !acces.getExpiredEntities().isEmpty()) {
            String noms = acces.getExpiredEntities().stream()
                    .map(e -> e.get("nom"))
                    .collect(java.util.stream.Collectors.joining(", "));
            throw new SchoolException(SchoolErrorCode.ABONNEMENT_EXPIRE,
                    "Votre offre a expiré pour : " + noms + ". Veuillez la renouveler pour vous connecter.");
        }

        List<java.util.Map<String, String>> expiredEntities = acces.getExpiredEntities();

        String accessToken = jwtUtil.generateAccessToken(existingUser.getEmail(), tokenRoles, expiredEntities);

        boolean isMultiRole = availableRoles.size() > 1;

        // Get children for parents
        List<AuthResponse.ChildInfo> children = null;
        if (availableRoles.contains("PARENT")) {
            children = utilisateursBusiness.getChildrenForParent(existingUser.getId());
        }

        return AuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getAccessTokenExpirationMillis())
                .userId(existingUser.getId())
                .userEmail(existingUser.getEmail())
                .username(existingUser.getNom() + " " + existingUser.getPrenom())
                .userType(selectedRole.toLowerCase())
                .userStatus(existingUser.getEtat())
                .availableRoles(availableRoles)
                .selectedRole(selectedRole)
                .multiRole(isMultiRole)
                .children(children)
                .expiredEntities(expiredEntities.isEmpty() ? null : expiredEntities)
                .build();
    }


    /**
     * Validate the refresh token and generate a new access token
     *
     * @param refreshToken The refresh token to validate
     * @return New access token if valid
     */
    public String refreshAccessToken(String refreshToken) {
        log.info("Refreshing access token using refresh token: {}", refreshToken);

        Optional<RefreshToken> token = refreshTokenBusiness.findByToken(refreshToken);
        if (token.isEmpty() || token.get().getExpiryDate().isBefore(Instant.now())) {
            log.warn("Invalid or expired refresh token: {}", refreshToken);
            throw new SchoolException(SchoolErrorCode.INVALID_TOKEN, "Refresh token is invalid or expired");
        }

        // Get the user associated with the refresh token
        Utilisateurs user = token.get().getUtilisateur();

        // Determine the roles for the user based on the type
        List<String> roles = new ArrayList<>();

        // Assign role based on user type
        if (user.isAdmin()) {
            roles.add("ROLE_ADMIN");
        } else if (user instanceof Professeurs) {
            roles.add("ROLE_PROFESSOR");
        } else if (user instanceof Eleves) {
            roles.add("ROLE_STUDENT");
        } else if (user instanceof Parents) {
            roles.add("ROLE_PARENT");
        } else if (user instanceof Repetiteurs) {
            roles.add("ROLE_TUTOR");
        } else {
            roles.add("ROLE_USER");  // Fallback role for unclassified users
        }

        // Generate a new access token with roles
        String newAccessToken = jwtUtil.generateAccessToken(user.getEmail(), roles);

        log.info("Access token refreshed successfully for user: {}", user.getEmail());
        return newAccessToken;
    }


    /**
     * Validates user data before registration or update
     * Checks for required fields, format validation, and security constraints
     *
     * @param utilisateur The user data to validate
     * @throws SchoolException if validation fails
     */
    private void validateUserData(Utilisateurs utilisateur) {
        // Validate required fields
        validateRequiredFields(utilisateur);

        // Validate email format
        validateEmailFormat(utilisateur.getEmail());

        // Validate password strength (when present)
        if (utilisateur.getPasseAccess() != null && !utilisateur.getPasseAccess().isEmpty()) {
            validatePasswordStrength(utilisateur.getPasseAccess());
        }

        // Validate phone number format (when present)
        if (utilisateur.getTelephone() != null && !utilisateur.getTelephone().isEmpty()) {
            validatePhoneNumber(utilisateur.getTelephone());
        }

        // Validate user type-specific requirements
        validateUserTypeRequirements(utilisateur);
    }

    /**
     * Validates that all required fields are present
     */
    private void validateRequiredFields(Utilisateurs utilisateur) {
        if (utilisateur.getEmail() == null || utilisateur.getEmail().trim().isEmpty()) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "Email is required");
        }

        if (utilisateur.getNom() == null || utilisateur.getNom().trim().isEmpty()) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "Last name is required");
        }

        if (utilisateur.getPrenom() == null || utilisateur.getPrenom().trim().isEmpty()) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "First name is required");
        }

        if (utilisateur.getPasseAccess() == null || utilisateur.getPasseAccess().trim().isEmpty()) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "Password is required");
        }
    }

    /**
     * Validates email format using regex pattern
     */
    private void validateEmailFormat(String email) {
        // Basic email validation regex
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

        if (!email.matches(emailRegex)) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "Invalid email format");
        }
    }

    /**
     * Validates password strength requirements
     */
    private void validatePasswordStrength(String password) {
        if (password.length() < 8) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT,
                    "Le mot de passe doit contenir au moins 8 caractères");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT,
                    "Le mot de passe doit contenir au moins une lettre majuscule");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT,
                    "Le mot de passe doit contenir au moins une lettre minuscule");
        }
        if (!password.matches(".*\\d.*")) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT,
                    "Le mot de passe doit contenir au moins un chiffre");
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT,
                    "Le mot de passe doit contenir au moins un caractère spécial (!@#$%^&*...)");
        }
    }

    /**
     * Validates phone number format for Cameroon and France
     */
    private void validatePhoneNumber(String phoneNumber) {
        // Remove any whitespace or formatting characters
        String cleanedPhone = phoneNumber.replaceAll("\\s+|-|\\(|\\)", "");

        // Check if the number matches Cameroon format
        // Cameroon: +237 followed by 8 digits or starts with 6 and has 9 digits total
        boolean validCameroon = cleanedPhone.matches("^(\\+237|00237)?[6-9]\\d{8}$");

        // Check if the number matches France format
        // France: +33 followed by 9 digits or starts with 0 and has 9 digits total
        boolean validFrance = cleanedPhone.matches("^(\\+33|0033)?[1-9]\\d{8}$");

        if (!validCameroon && !validFrance) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT,
                    "Invalid phone number format. Please enter a valid Cameroon or France phone number.");
        }

        // Additional check: If the phone number doesn't include the country code,
        // ensure it has the appropriate format for each country
        if (!cleanedPhone.startsWith("+") && !cleanedPhone.startsWith("00")) {
            // For Cameroon, should start with 6 or other valid prefix and have 9 digits total
            boolean validCameroonFormat = cleanedPhone.matches("^6\\d{8}$");

            // For France, should start with 0 and have 10 digits total
            boolean validFranceFormat = cleanedPhone.matches("^0[1-9]\\d{8}$");

            if (!validCameroonFormat && !validFranceFormat) {
                throw new SchoolException(SchoolErrorCode.INVALID_INPUT,
                        "Without country code, phone number should follow Cameroon format (6XXXXXXXX) or France format (0XXXXXXXXX)");
            }
        }
    }

    /**
     * Validates user type-specific requirements
     */
    private void validateUserTypeRequirements(Utilisateurs utilisateur) {
        // Implement any additional validation logic based on user type
        if (utilisateur instanceof Professeurs) {
            // Add specific validations for professors if needed
        }
        // Add other user type checks as necessary
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

    public void changePassword(String userEmail, ChangePasswordRequest request) {
        log.info("Processing change-password for: {}", userEmail);

        Utilisateurs user = utilisateursBusiness.avoirUtilisateurParEmail(userEmail);
        if (user == null) {
            throw new SchoolException(SchoolErrorCode.NOT_FOUND, "User not found");
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasseAccess())) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "Mot de passe actuel incorrect");
        }

        validatePasswordStrength(request.getNewPassword());

        user.setPasseAccess(passwordEncoder.encode(request.getNewPassword()));
        utilisateursBusiness.mettreUtilisateurAJour(user);
        log.info("Password changed successfully for: {}", userEmail);
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
        return registerUser(utilisateur);
    }
}