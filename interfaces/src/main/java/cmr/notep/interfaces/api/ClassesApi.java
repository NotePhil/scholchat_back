package cmr.notep.interfaces.api;

import cmr.notep.interfaces.modeles.Classes;
import lombok.NonNull;
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
    Classes creerClasse(@NonNull @RequestBody Classes classes);

    @PutMapping(
            path = "/{idClasse}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    Classes modifierClasse(
            @NonNull @PathVariable("idClasse") String idClasse,
            @NonNull @RequestBody Classes classeModifiee
    );

    @DeleteMapping("/{idClasse}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void supprimerClasse(@NonNull @PathVariable("idClasse") String idClasse);

    @GetMapping(
            path = "/{idClasse}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    Classes obtenirClasseParId(@NonNull @PathVariable("idClasse") String idClasse);

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<Classes> obtenirToutesLesClasses();

    @PostMapping(
            path = "/{idClasse}/eleves/{idEleve}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    Classes ajouterEleve(
            @NonNull @PathVariable("idClasse") String idClasse,
            @NonNull @PathVariable("idEleve") String idEleve
    );

    @DeleteMapping(
            path = "/{idClasse}/eleves/{idEleve}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    Classes supprimerEleve(
            @NonNull @PathVariable("idClasse") String idClasse,
            @NonNull @PathVariable("idEleve") String idEleve
    );

    @PostMapping(
            path = "/{idClasse}/parents/{idParent}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    Classes ajouterParent(
            @NonNull @PathVariable("idClasse") String idClasse,
            @NonNull @PathVariable("idParent") String idParent
    );

    @DeleteMapping(
            path = "/{idClasse}/parents/{idParent}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    Classes supprimerParent(
            @NonNull @PathVariable("idClasse") String idClasse,
            @NonNull @PathVariable("idParent") String idParent
    );
}