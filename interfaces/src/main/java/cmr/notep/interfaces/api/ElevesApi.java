package cmr.notep.interfaces.api;

import cmr.notep.interfaces.modeles.Eleves;
import lombok.NonNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/profil-eleves")
public interface ElevesApi {

    @GetMapping(
            path = "/{idEleve}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    Eleves avoirEleve(@NonNull @PathVariable String idEleve);

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<Eleves> avoirToutEleves();

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    Eleves posterEleve(@NonNull @RequestBody Eleves Eleve);
}
