package cmr.notep.interfaces.api;

import cmr.notep.interfaces.modeles.MotifRejetClasse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/motifsRejetClasses")
public interface MotifsRejetClasseApi {
    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    MotifRejetClasse creerMotifRejetClasse(@RequestBody MotifRejetClasse motifRejetClasse);

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<MotifRejetClasse> obtenirTousMotifsRejetClasse();

    @DeleteMapping(
            path = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    void supprimerMotifRejetClasse(@PathVariable String id);
}