package cmr.notep.interfaces.api;

import cmr.notep.interfaces.modeles.Etablissement;
import cmr.notep.interfaces.modeles.Utilisateurs;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/etablissements")
public interface EtablissementApi {
    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    Etablissement creerEtablissement(@NonNull @RequestBody Etablissement etablissement);

    @PutMapping(
            path = "/{idEtablissement}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    Etablissement modifierEtablissement(
            @NonNull @PathVariable("idEtablissement") String idEtablissement,
            @NonNull @RequestBody Etablissement etablissementModifie
    );

    @DeleteMapping("/{idEtablissement}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void supprimerEtablissement(@NonNull @PathVariable("idEtablissement") String idEtablissement);

    @GetMapping(
            path = "/{idEtablissement}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    Etablissement obtenirEtablissementParId(@NonNull @PathVariable("idEtablissement") String idEtablissement);

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<Etablissement> obtenirTousLesEtablissements();
    
    @GetMapping(
            path = "/gestionnaire/{gestionnaireId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<Etablissement> obtenirEtablissementsParGestionnaire(@NonNull @PathVariable("gestionnaireId") String gestionnaireId);

    @GetMapping(
            path = "/{idEtablissement}/gestionnaire",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    Utilisateurs obtenirGestionnaireEtablissement(@NonNull @PathVariable("idEtablissement") String idEtablissement);
}
