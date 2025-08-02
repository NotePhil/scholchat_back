package cmr.notep.interfaces.api;

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

    @GetMapping("/{parentId}/enfants")
    @ResponseStatus(HttpStatus.OK)
    List<Eleves> obtenirEnfants(@PathVariable String parentId);
}