package cmr.notep.interfaces.api;

import cmr.notep.interfaces.modeles.Evenement;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/evenements")
public interface EvenementApi {

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    Evenement creerEvenement(@NonNull @RequestBody Evenement evenement);

    @PutMapping(
            path = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    Evenement modifierEvenement(
            @NonNull @PathVariable("id") String id,
            @NonNull @RequestBody Evenement evenementModifie
    );

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void supprimerEvenement(@NonNull @PathVariable("id") String id);

    @GetMapping(
            path = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    Evenement obtenirEvenementParId(@NonNull @PathVariable("id") String id);

    @GetMapping(
            path = "/canal/{canalId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<Evenement> obtenirEvenementsParCanal(@NonNull @PathVariable("canalId") String canalId);
}