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

    public AuthService(AuthBusiness authBusiness, ActivationService activationService) {
        this.authBusiness = authBusiness;
        this.activationService = activationService;
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

    @Override
    public AuthResponse switchRole(@NonNull LoginDto switchRequest) {
        log.info("Switching role for user: {} to {}", switchRequest.getEmail(), switchRequest.getSelectedRole());
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
        log.info("Resetting password for user with token: {}", request.getToken());
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
        authBusiness.registerPassword(request);
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Changing password for user: {}", userEmail);
        authBusiness.changePassword(userEmail, request);
    }
}