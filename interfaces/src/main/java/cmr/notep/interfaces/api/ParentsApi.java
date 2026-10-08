package cmr.notep.interfaces.api;

import cmr.notep.interfaces.dto.ParentSummaryDto;
import cmr.notep.interfaces.modeles.Eleves;
import cmr.notep.interfaces.modeles.Parents;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/parents")
public interface ParentsApi {
    @GetMapping(
            path = "/{idProfilParent}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    Parents avoirParent(@NonNull @PathVariable String idProfilParent);

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<Parents> avoirToutParents();

    @GetMapping(
            path = "/summary",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<ParentSummaryDto> avoirToutParentsSummary();

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    Parents posterParent(@NonNull @RequestBody Parents profilParent);

    @PatchMapping(
            path = "/{idProfilParent}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    Parents modifierParentPartiellement(
            @NonNull @PathVariable String idProfilParent,
            @NonNull @RequestBody Parents partialParent);


    @PostMapping("/{parentId}/enfants/{eleveId}")
    @ResponseStatus(HttpStatus.OK)
    void ajouterEnfant(
            @PathVariable String parentId,
            @PathVariable String eleveId);

    @DeleteMapping("/{parentId}/enfants/{eleveId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void retirerEnfant(
            @PathVariable String parentId,
            @PathVariable String eleveId);

    @GetMapping("/professeur/{professeurId}")
    List<ParentSummaryDto> avoirParentsPourProfesseur(@PathVariable String professeurId);

    @GetMapping("/{parentId}/enfants")
    @ResponseStatus(HttpStatus.OK)
    List<Eleves> obtenirEnfants(@PathVariable String parentId);

    /** Enfants du parent et état de leurs inscriptions dans les classes (APPROUVEE / EN_ATTENTE / REJETEE). */
    @GetMapping(path = "/{parentId}/enfants/statuts", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    List<cmr.notep.interfaces.modeles.EnfantStatut> obtenirStatutsEnfants(@PathVariable String parentId);

    /** Inscrit un nouvel enfant ({prenom, nom, codeClasse}) : élève créé + demande d'accès à sa classe. */
    @PostMapping(path = "/{parentId}/enfants/inscription", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    cmr.notep.interfaces.modeles.EnfantInscription inscrireEnfant(
            @PathVariable String parentId,
            @RequestBody cmr.notep.interfaces.modeles.EnfantInscription enfant);
}