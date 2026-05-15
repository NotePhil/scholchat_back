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
    Professeurs avoirProfesseur(@NonNull @PathVariable String idProfesseur);

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<Professeurs> avoirToutProfesseurs();

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    Professeurs posterProfesseur(@NonNull @RequestBody Professeurs professeur);

    @PatchMapping(
            path = "/{idProfesseur}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    Professeurs modifierProfesseurPartiellement(
            @NonNull @PathVariable String idProfesseur,
            @NonNull @RequestBody Professeurs professeur);

    @GetMapping(
            path = "/matricule/{matriculeProfesseur}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    Professeurs avoirProfesseurParMatricule(@NonNull @PathVariable String matriculeProfesseur);

    @GetMapping(
            path = "/classe/{classeId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<Professeurs> avoirProfesseursParClasse(@NonNull @PathVariable String classeId);

    @GetMapping(
            path = "/moderateur/{moderateurId}/collaborateurs",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<Professeurs> avoirCollaborateursProfesseur(@NonNull @PathVariable String moderateurId);
}