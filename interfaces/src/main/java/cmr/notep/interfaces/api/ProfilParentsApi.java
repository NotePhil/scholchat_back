package cmr.notep.interfaces.api;

import cmr.notep.interfaces.modeles.ProfilParents;
import lombok.NonNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/profilparents")
public interface ProfilParentsApi {
    @GetMapping(
            path = "/{idProfilParent}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ProfilParents avoirProfilParent(@NonNull @PathVariable String idProfilParent);

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<ProfilParents> avoirToutProfilParents();

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    ProfilParents posterProfilParent(@NonNull @RequestBody ProfilParents profilParent);
}