package cmr.notep.interfaces.api;

import cmr.notep.interfaces.modeles.Parents;
import lombok.NonNull;
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
}