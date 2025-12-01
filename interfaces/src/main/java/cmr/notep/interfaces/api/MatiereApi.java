package cmr.notep.interfaces.api;

import cmr.notep.interfaces.modeles.Matiere;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/matieres")
public interface MatiereApi {

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    Matiere creerMatiere(@NonNull @RequestBody Matiere matiere);

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<Matiere> obtenirToutesMatieres();

    @GetMapping(
            path = "/{nom}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    Matiere obtenirMatiereParNom(@NonNull @PathVariable("nom") String nomMatiere);

    @PutMapping(
            path = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    Matiere modifierMatiere(@NonNull @PathVariable("id") String id, @NonNull @RequestBody Matiere matiere);

    @DeleteMapping(
            path = "/{id}"
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void supprimerMatiere(@NonNull @PathVariable("id") String id);
}