package cmr.notep.interfaces.api;

import cmr.notep.interfaces.modeles.Professeur;
import lombok.NonNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/professeurs")
public interface ProfesseurApi {

    @GetMapping(
            path = "/{idProfesseur}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    Professeur obtenirProfesseurParId(@NonNull @PathVariable Long idProfesseur);

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<Professeur> obtenirTousLesProfesseurs();

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    Professeur creerProfesseur(@NonNull @RequestBody Professeur professeur);
}
