package cmr.notep.business.business;

import cmr.notep.business.config.JwtConfig;
import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.services.ActivationEmailService;
import cmr.notep.business.utils.JwtUtil;
import cmr.notep.interfaces.dto.LoginDto;
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


public AuthBusiness(PasswordEncoder passwordEncoder,UtilisateursBusiness utilisateursBusiness, JwtUtil jwtUtil, JwtConfig jwtConfig, ActivationEmailService activationEmailService, RefreshTokenBusiness refreshTokenBusiness) {
    this.passwordEncoder = passwordEncoder;
    this.utilisateursBusiness = utilisateursBusiness;
    this.jwtUtil = jwtUtil;
    this.jwtConfig = jwtConfig;
    this.activationEmailService = activationEmailService;
    this.refreshTokenBusiness = refreshTokenBusiness;

}

    /**
     * Register a new user account
     *
     * @param utilisateur User information for registration
     * @return Registration result message
     */
    public String registerUser(Utilisateurs utilisateur) {
        log.info("Processing user registration request for email: {}", utilisateur.getEmail());

        // Check if email already exists

        Utilisateurs existingUser = utilisateursBusiness.avoirUtilisateurParEmail(utilisateur.getEmail());
        if (existingUser == null) {
            log.warn("Email already registered: {}", utilisateur.getEmail());
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "Email already registered");
        }

        existingUser.setPasseAccess(utilisateur.getPasseAccess());
        // Validate user data
        validateUserData(existingUser);

        // Encode password before saving
        log.debug("Encoding password for user: {}", utilisateur.getEmail());
        existingUser.setPasseAccess(passwordEncoder.encode(utilisateur.getPasseAccess()));
        existingUser.setEtat(EtatUtilisateur.ACTIVE);

        // Save user - will handle activation email internally
        utilisateursBusiness.mettreUtilisateurAJour(existingUser);

        log.info("User registration completed successfully for: {}", utilisateur.getEmail());
        return "User registered successfully. Please check your email for activation instructions.";
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

        // Verify password
        if (!passwordEncoder.matches(loginRequest.getPassword(), existingUser.getPasseAccess())) {
            log.warn("Invalid login attempt for user: {}", loginRequest.getEmail());
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "Invalid email or password");
        }

        // Check if user account is active
        if (existingUser.getEtat() != EtatUtilisateur.ACTIVE) {
            log.warn("Login attempt for inactive account: {}", loginRequest.getEmail());

            if (existingUser.getEtat() == EtatUtilisateur.PENDING) {
                throw new SchoolException(
                        SchoolErrorCode.INVALID_STATE,
                        "User account is still pending activation"
                );
            } else {
                throw new SchoolException(
                        SchoolErrorCode.INACTIVE_USER,
                        "User account is inactive"
                );
            }
        }

        // Determine the roles for the user based on the type
        List<String> roles = new ArrayList<>();

        // Assign role based on user type
        if (existingUser.isAdmin()) {
            roles.add("ROLE_ADMIN");
        } else if (existingUser instanceof Professeurs) {
            roles.add("ROLE_PROFESSOR");
        } else if (existingUser instanceof Eleves) {
            roles.add("ROLE_STUDENT");
        } else if (existingUser instanceof Parents) {
            roles.add("ROLE_PARENT");
        } else if (existingUser instanceof Repetiteurs) {
            roles.add("ROLE_TUTOR");
        } else {
            roles.add("ROLE_USER");  // Fallback role for unclassified users
        }

        // Generate tokens with roles
        String accessToken = jwtUtil.generateAccessToken(existingUser.getEmail(), roles);
        String refreshToken = refreshTokenBusiness.createRefreshToken(existingUser);

        log.info("User logged in successfully: {}", loginRequest.getEmail());

        // Build the response using the builder pattern
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getAccessTokenExpirationMillis())
                .userId(existingUser.getId())
                .userEmail(existingUser.getEmail())
                .username(existingUser.getNom() + " " + existingUser.getPrenom())
                .userType(existingUser.getClass().getSimpleName().toLowerCase())
                .userStatus(existingUser.getEtat())
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
        // Password must be at least 8 characters
        if (password.length() < 8) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT,
                    "Password must be at least 8 characters long");
        }

        // Check for at least one uppercase letter
        if (!password.matches(".*[A-Z].*")) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT,
                    "Password must contain at least one uppercase letter");
        }

        // Check for at least one lowercase letter
        if (!password.matches(".*[a-z].*")) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT,
                    "Password must contain at least one lowercase letter");
        }

        // Check for at least one digit
        if (!password.matches(".*\\d.*")) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT,
                    "Password must contain at least one digit");
        }

        // Check for at least one special character
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) { throw new SchoolException(SchoolErrorCode.INVALID_INPUT,
                "Password must contain at least one special character");
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
}