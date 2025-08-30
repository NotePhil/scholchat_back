package cmr.notep.interfaces.api;

import cmr.notep.interfaces.modeles.Classes;
import cmr.notep.interfaces.modeles.HistoActivation;
import cmr.notep.interfaces.modeles.Utilisateurs;
import cmr.notep.modele.DroitPublication;
import cmr.notep.modele.EtatClasse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/classes")
public interface ClassesApi {

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    Classes creerClasse(@RequestBody Classes classes);

    @PutMapping(
            path = "/{idClasse}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    Classes modifierClasse(
            @PathVariable("idClasse") String idClasse,
            @RequestBody Classes classeModifiee
    );

    @PatchMapping("/{idClasse}/approve")
    @ResponseStatus(HttpStatus.OK)
    Classes approuverClasse(@PathVariable("idClasse") String idClasse);

    @PatchMapping("/{idClasse}/reject")
    @ResponseStatus(HttpStatus.OK)
    Classes rejeterClasse(
            @PathVariable("idClasse") String idClasse,
            @RequestParam String motif);

    @GetMapping(path = "/by-status", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    List<Classes> obtenirClassesParEtat(@RequestParam(required = false) EtatClasse etat);

    @DeleteMapping("/{idClasse}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void supprimerClasse(@PathVariable("idClasse") String idClasse);

    @GetMapping(
            path = "/{idClasse}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    Classes obtenirClasseParId(@PathVariable("idClasse") String idClasse);

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<Classes> obtenirToutesLesClasses();

    @PatchMapping(
            path = "/{idClasse}/publication-rights",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    Classes modifierDroitPublication(
            @PathVariable("idClasse") String idClasse,
            @RequestParam DroitPublication droitPublication);

    @GetMapping(
            path = "/{idClasse}/activation-history",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<HistoActivation> obtenirHistoriqueActivation(
            @PathVariable("idClasse") String idClasse);

    // NEW MODERATOR MANAGEMENT ENDPOINTS
    @PatchMapping(
            path = "/{idClasse}/moderator/{idModerator}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    Classes assignerModerator(
            @PathVariable("idClasse") String idClasse,
            @PathVariable("idModerator") String idModerator);

    @DeleteMapping(
            path = "/{idClasse}/moderator",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    Classes retirerModerator(@PathVariable("idClasse") String idClasse);


//    @GetMapping(
//            path = "/{idClasse}/users",
//            produces = MediaType.APPLICATION_JSON_VALUE
//    )
//    @ResponseStatus(HttpStatus.OK)
//    List<Utilisateurs> obtenirUtilisateursParClasse(@PathVariable("idClasse") String idClasse);

}