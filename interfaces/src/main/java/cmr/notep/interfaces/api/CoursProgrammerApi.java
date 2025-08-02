package cmr.notep.interfaces.api;

import cmr.notep.interfaces.modeles.CoursProgrammer;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/cours-programmes")
public interface CoursProgrammerApi {

    @PostMapping
    CoursProgrammer programmerCours(@RequestBody CoursProgrammer coursProgrammer);

    @GetMapping("/by-cours/{coursId}")
    List<CoursProgrammer> obtenirProgrammationParCours(@PathVariable String coursId);

    @GetMapping("/by-classe/{classeId}")
    List<CoursProgrammer> obtenirProgrammationParClasse(@PathVariable String classeId);
}