package cmr.notep.interfaces.api;

import cmr.notep.interfaces.modeles.Offre;
import cmr.notep.modele.TypeCibleOffre;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/offres")
public interface OffreApi {

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    List<Offre> listerOffres(@RequestParam(required = false) TypeCibleOffre cible,
                              @RequestParam(required = false, defaultValue = "false") boolean toutes);

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    Offre obtenirOffreParId(@PathVariable("id") String id);

    // Ecriture reservee ADMIN : voir @PreAuthorize sur l'implementation OffreService (business/impl),
    // le module interfaces ne depend pas de spring-security.
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    Offre creerOffre(@RequestBody Offre offre);

    @PutMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    Offre modifierOffre(@PathVariable("id") String id, @RequestBody Offre offre);

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void desactiverOffre(@PathVariable("id") String id);
}
