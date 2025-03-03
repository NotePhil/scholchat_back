package cmr.notep.business.impl;

import cmr.notep.business.business.AuthBusiness;
import cmr.notep.interfaces.api.AuthApi;
import cmr.notep.interfaces.dto.LoginDto;
import cmr.notep.interfaces.modeles.AuthResponse;
import cmr.notep.interfaces.modeles.Utilisateurs;
import cmr.notep.business.services.ActivationService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class AuthService implements AuthApi {

    private final AuthBusiness authBusiness;
    private final ActivationService activationService;

    @Override
    public String registerUser(@NonNull Utilisateurs utilisateur) {
        log.info("Registering new user: {}", utilisateur.getEmail());
        return authBusiness.registerUser (utilisateur);
    }

    @Override
    public AuthResponse loginUser(@NonNull LoginDto loginDto) {
        log.info("Logging in user: {}", loginDto.getEmail());
        return authBusiness.loginUser(loginDto);
    }


    @Override
    public Utilisateurs activerUtilisateur(String activationToken) {
        log.info("Activating user with token: {}", activationToken);
        return activationService.activerUtilisateur(activationToken);
    }

    @Override
    public String refreshToken(String refreshToken) {
        log.info("Refreshing token: {}", refreshToken);
        return authBusiness.refreshAccessToken(refreshToken);
    }
}