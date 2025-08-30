package cmr.notep.interfaces.api;

import cmr.notep.interfaces.modeles.HistoActivation;
import cmr.notep.modele.EtatClasse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/histo-activations")
public interface HistoActivationApi {
    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    HistoActivation creerEntreeActivation(@RequestBody HistoActivation histoActivation);

    @PatchMapping(
            path = "/{id}/desactivation",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    HistoActivation desactiverEntree(
            @PathVariable("id") String id,
            @RequestParam String motif);

    @GetMapping(
            path = "/classe/{classeId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<HistoActivation> obtenirHistoriqueParClasse(@PathVariable("classeId") String classeId);

    @GetMapping(
            path = "/utilisateur/{utilisateurId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<HistoActivation> obtenirHistoriqueParUtilisateur(@PathVariable("utilisateurId") String utilisateurId);

    @GetMapping(
            path = "/actives",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<HistoActivation> obtenirActivationsActives();

    @GetMapping(
            path = "/etat/{etatClasse}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<HistoActivation> obtenirParEtatClasse(@PathVariable("etatClasse") EtatClasse etatClasse);
}