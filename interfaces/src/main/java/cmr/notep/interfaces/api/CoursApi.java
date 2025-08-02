package cmr.notep.interfaces.api;

import cmr.notep.interfaces.modeles.Cours;
import cmr.notep.modele.EtatCours;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/cours")
public interface CoursApi {

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    Cours creerCours(@NonNull @RequestBody Cours cours);

    @GetMapping(
            path = "/professeur/{professeurId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<Cours> obtenirCoursParProfesseur(@NonNull @PathVariable String professeurId);

    @GetMapping(
            path = "/matiere/{matiereId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<Cours> obtenirCoursParMatiere(@NonNull @PathVariable String matiereId);

    @GetMapping(
            path = "/etat/{etat}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<Cours> obtenirCoursParEtat(@NonNull @PathVariable EtatCours etat);
}