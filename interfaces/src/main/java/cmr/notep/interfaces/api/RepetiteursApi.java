package cmr.notep.interfaces.api;

import cmr.notep.interfaces.modeles.Repetiteurs;
import lombok.NonNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API REST pour les répétiteurs.
 */
@RequestMapping("/repetiteurs")
public interface RepetiteursApi {

    @GetMapping(
            path = "/{idRepetiteur}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    Repetiteurs obtenirRepetiteurParId(@NonNull @PathVariable Long idRepetiteur);

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<Repetiteurs> obtenirTousLesRepetiteurs();

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    Repetiteurs creerRepetiteur(@NonNull @RequestBody Repetiteurs repetiteur);
}
