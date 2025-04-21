package cmr.notep.interfaces.api;

import cmr.notep.interfaces.modeles.IUtilisateurs;
import cmr.notep.interfaces.modeles.Utilisateurs;
import lombok.NonNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/utilisateurs")
public interface UtilisateursApi {
    @GetMapping(
            path = "/{idUtilisateur}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    Utilisateurs avoirUtilisateur(@NonNull @PathVariable(name = "idUtilisateur") String idUtilisateur);

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<Utilisateurs> avoirToutUtilisateurs();

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    Utilisateurs posterUtilisateur(@NonNull @RequestBody Utilisateurs utilisateur);


    @PostMapping(
            path = "/regenerate-activation",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    Utilisateurs regenererActivationEmail(@RequestParam String email);

    @PostMapping(
            path = "/professors/{professorId}/validate",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    Utilisateurs validerProfesseur(
            @NonNull @PathVariable(name = "professorId") String professorId
    );

    @PostMapping(
            path = "/professeurs/{professorId}/rejet",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    Utilisateurs rejeterProfesseur(
            @PathVariable String professorId,
            @RequestParam String codeErreur,
            @RequestParam(required = false) String motifSupplementaire);
    @GetMapping(
            path = "/professors/pending",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<Utilisateurs> avoirProfesseursEnAttente();
}