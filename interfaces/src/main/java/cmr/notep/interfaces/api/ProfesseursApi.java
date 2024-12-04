package cmr.notep.interfaces.api;

import cmr.notep.interfaces.modeles.Professeurs;
import lombok.NonNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/professeurs")
public interface ProfesseursApi {

    @GetMapping(
            path = "/{idProfesseur}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    Professeurs obtenirProfesseurParId(@NonNull @PathVariable String idProfesseur);

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<Professeurs> obtenirTousLesProfesseurs();

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    Professeurs creerProfesseur(@NonNull @RequestBody Professeurs professeur);
}
