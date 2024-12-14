package cmr.notep.interfaces.api;

import cmr.notep.interfaces.modeles.ProfilEleves;
import lombok.NonNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/profil-eleves")
public interface ProfilElevesApi {

    @GetMapping(
            path = "/{idProfilEleve}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ProfilEleves avoirProfilEleve(@NonNull @PathVariable String idProfilEleve);

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<ProfilEleves> avoirToutProfilEleves();

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    ProfilEleves posterProfilEleve(@NonNull @RequestBody ProfilEleves profilEleve);
}
