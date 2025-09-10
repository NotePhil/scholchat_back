package cmr.notep.interfaces.api;

import cmr.notep.interfaces.modeles.MotifRejet;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/motifsRejets")
public interface MotifsRejetApi {
    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    MotifRejet creerMotifRejet(@RequestBody MotifRejet motifRejet);

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<MotifRejet> obtenirTousMotifsRejet();

    @GetMapping(
            path = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    MotifRejet obtenirMotifParId(@PathVariable String id);

    @PatchMapping(
            path = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    MotifRejet modifierMotifRejet(@PathVariable String id, @RequestBody MotifRejet motifRejet);

    @DeleteMapping(
            path = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    void supprimerMotifRejet(@PathVariable String id);
}