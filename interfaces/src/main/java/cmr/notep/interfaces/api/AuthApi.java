package cmr.notep.interfaces.api;

import cmr.notep.interfaces.dto.LoginDto;
import cmr.notep.interfaces.dto.PasswordResetRequest;
import cmr.notep.interfaces.dto.PasswordSetupRequest;
import cmr.notep.interfaces.modeles.AuthResponse;
import cmr.notep.interfaces.modeles.Utilisateurs;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public interface AuthApi {

    @PostMapping(
            path = "/register",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void registerUser(@NonNull @RequestBody Utilisateurs utilisateur);

    @PostMapping(
            path = "/reset-password-request",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    void requestPasswordReset(@RequestParam String email);


    @PostMapping(
            path = "/reset-password",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    void resetPassword(@RequestBody PasswordResetRequest request);

    @PostMapping(
            path = "/login",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    AuthResponse loginUser(@NonNull @RequestBody LoginDto utilisateur);

    @PostMapping(
            path = "/switch-role",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    AuthResponse switchRole(@NonNull @RequestBody LoginDto switchRequest);
    @PostMapping(
            path = "/registerPassword",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    void registerPassword(@RequestBody PasswordSetupRequest request);

    @GetMapping(
            path = "/users/byEmail",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    Utilisateurs getUtilisateurByEmailWithToken(
            @RequestParam String email,
            @RequestParam String token
    );

    @PostMapping(
            path = "/users/register",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    Utilisateurs registerUserWithToken(
            @RequestBody Utilisateurs utilisateur,
            @RequestParam String token
    );

    @PostMapping(
            path = "/activate",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    Utilisateurs activerUtilisateur(@RequestParam String activationToken);

}