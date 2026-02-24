package cmr.notep.interfaces.api;

import cmr.notep.interfaces.modeles.Gestionnaires;
import lombok.NonNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/gestionnaires")
public interface GestionnairesApi {
    
    @GetMapping(
            path = "/{idGestionnaire}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    Gestionnaires avoirGestionnaire(@NonNull @PathVariable(name = "idGestionnaire") String idGestionnaire);

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<Gestionnaires> avoirTousGestionnaires();
}
