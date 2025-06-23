package cmr.notep.interfaces.api;

import cmr.notep.interfaces.modeles.Classes;
import cmr.notep.interfaces.modeles.Utilisateurs;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/acceder")
public interface AccederApi {

    @PostMapping(
            path = "/{utilisateurId}/{classeId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    void donnerAcces(
            @PathVariable String utilisateurId,
            @PathVariable String classeId
    );

    @DeleteMapping(
            path = "/{utilisateurId}/{classeId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void retirerAcces(
            @PathVariable String utilisateurId,
            @PathVariable String classeId
    );

    @GetMapping(
            path = "/classes/{classeId}/utilisateurs",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<Utilisateurs> obtenirUtilisateursAvecAcces(
            @PathVariable String classeId
    );

    @GetMapping(
            path = "/utilisateurs/{utilisateurId}/classes",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<Classes> obtenirClassesAccessibles(
            @PathVariable String utilisateurId
    );
}
