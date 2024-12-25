package cmr.notep.interfaces.api;

import cmr.notep.interfaces.modeles.Utilisateurs;
import lombok.NonNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/utilisateurs")
public interface UtilisateursApi {
    @GetMapping(
            path = "/{idUtilisateur}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    Utilisateurs avoirUtilisateur(@NonNull @RequestParam String idUtilisateur);

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<Utilisateurs> avoirToutUtilisateurs();

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    Utilisateurs posterUtilisateur(@NonNull @RequestBody Utilisateurs utilisateur);
}