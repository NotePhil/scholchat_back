package cmr.notep.interfaces.api;

import cmr.notep.interfaces.dto.ClasseAvecDroitDto;
import cmr.notep.interfaces.modeles.Classes;
import cmr.notep.interfaces.modeles.Utilisateurs;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/droits-publication")
public interface DroitPublicationApi {

    @PostMapping(
            path = "/{utilisateurId}/{classeId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    void attribuerDroitPublication(
            @PathVariable String utilisateurId,
            @PathVariable String classeId,
            @RequestParam boolean peutPublier,
            @RequestParam boolean peutModerer
    );

    @PutMapping(
            path = "/{utilisateurId}/{classeId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    void modifierDroitPublication(
            @PathVariable String utilisateurId,
            @PathVariable String classeId,
            @RequestParam boolean peutPublier,
            @RequestParam boolean peutModerer
    );

    @DeleteMapping(
            path = "/{utilisateurId}/{classeId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void retirerDroitPublication(
            @PathVariable String utilisateurId,
            @PathVariable String classeId
    );

    @GetMapping(
            path = "/classes/{classeId}/utilisateurs",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<Utilisateurs> obtenirUtilisateursAvecDroitPublication(
            @PathVariable String classeId
    );

    @GetMapping(
            path = "/utilisateurs/{utilisateurId}/classes",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<Classes> obtenirClassesAvecDroitPublication(
            @PathVariable String utilisateurId
    );

    /**
     * Returns classes where the user has publication rights,
     * each enriched with peutPublier and peutModerer flags.
     * peutModerer=true  → user created/moderates the class
     * peutModerer=false → rights were granted by someone else
     */
    @GetMapping(
            path = "/utilisateurs/{utilisateurId}/classes-avec-droits",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<ClasseAvecDroitDto> obtenirClassesAvecDroitsDetail(
            @PathVariable String utilisateurId
    );
}