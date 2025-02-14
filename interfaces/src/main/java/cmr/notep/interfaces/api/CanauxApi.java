package cmr.notep.interfaces.api;

import cmr.notep.interfaces.modeles.Canal;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/canaux")
public interface CanauxApi {

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    Canal creerCanal(@NonNull @RequestBody Canal canal);

    @PutMapping(
            path = "/{idCanal}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    Canal modifierCanal(
            @NonNull @PathVariable("idCanal") String idCanal,
            @NonNull @RequestBody Canal canalModifie
    );

    @DeleteMapping("/{idCanal}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void supprimerCanal(@NonNull @PathVariable("idCanal") String idCanal);

    @GetMapping(
            path = "/{idCanal}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    Canal obtenirCanalParId(@NonNull @PathVariable("idCanal") String idCanal);

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<Canal> obtenirTousLesCanaux();

    @GetMapping(
            path = "/classe/{idClasse}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<Canal> obtenirCanauxParClasse(@NonNull @PathVariable("idClasse") String idClasse);

    @GetMapping(
            path = "/professeur/{idProfesseur}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<Canal> obtenirCanauxParProfesseur(@NonNull @PathVariable("idProfesseur") String idProfesseur);
}
