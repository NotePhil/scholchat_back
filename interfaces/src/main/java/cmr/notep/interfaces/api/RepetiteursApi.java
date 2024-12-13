package cmr.notep.interfaces.api;

import cmr.notep.interfaces.modeles.Repetiteurs;
import lombok.NonNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/repetiteurs")
public interface RepetiteursApi {
    @GetMapping(
            path = "/{idRepetiteur}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    Repetiteurs avoirRepetiteur(@NonNull @PathVariable String idRepetiteur);

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<Repetiteurs> avoirToutRepetiteurs();

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    Repetiteurs posterRepetiteur(@NonNull @RequestBody Repetiteurs repetiteur);
}
