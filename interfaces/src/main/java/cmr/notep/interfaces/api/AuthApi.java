package cmr.notep.interfaces.api;

import cmr.notep.interfaces.dto.LoginDto;
import cmr.notep.interfaces.modeles.AuthResponse;
import cmr.notep.interfaces.modeles.Utilisateurs;
import lombok.NonNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public interface AuthApi {

    @PostMapping(
            path = "/register",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    String registerUser(@NonNull @RequestBody Utilisateurs utilisateur);

    @PostMapping(
            path = "/login",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    AuthResponse loginUser(@NonNull @RequestBody LoginDto utilisateur);


    @PostMapping(
            path = "/activate",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    Utilisateurs activerUtilisateur(@RequestParam String activationToken);

    @PostMapping(
            path = "/refresh",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    String refreshToken(@RequestParam String refreshToken);
}