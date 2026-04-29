package cmr.notep.interfaces.api;

import cmr.notep.interfaces.modeles.Classes;
import cmr.notep.interfaces.modeles.DemandeAccesDto;
import cmr.notep.interfaces.modeles.UtilisateurSimpleDto;
import cmr.notep.interfaces.modeles.Utilisateurs;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/acceder")
public interface AccederApi {
    @PostMapping(
            path = "/demandes",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    void demanderAcces(
            @RequestParam String utilisateurId,
            @RequestParam String classeId,
            @RequestParam String codeActivation,
            @RequestParam(required = false, defaultValue = "false") boolean estParent,
            @RequestParam(required = false) String eleveAssocieId
    );

    @PostMapping(
            path = "/demandes/{demandeId}/approve",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    void validerDemandeAcces(
            @PathVariable String demandeId
    );

    // Dans AccederApi.java
    @GetMapping(
            path = "/moderator/{moderatorId}/demandes",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<DemandeAccesDto> obtenirDemandesAccesPourModerateur(
            @PathVariable String moderatorId
    );

    @PostMapping(
            path = "/demandes/{demandeId}/reject",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    void rejeterDemandeAcces(
            @PathVariable String demandeId,
            @RequestParam String motifRejet
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
            path = "/classes/utilisateurs",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<UtilisateurSimpleDto> obtenirUtilisateursAvecAcces(
            @RequestParam List<String> classeIds
    );

    @GetMapping(
            path = "/classes/{classeId}/utilisateurs",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<UtilisateurSimpleDto> obtenirUtilisateursAvecAcces(
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

    @GetMapping(
            path = "/classes/{classeId}/demandes",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<DemandeAccesDto> obtenirDemandesAccesPourClasse(
            @PathVariable String classeId
    );
}