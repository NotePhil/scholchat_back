package cmr.notep.interfaces.api;

import cmr.notep.interfaces.dto.ContratActionDto;
import cmr.notep.interfaces.dto.RenouvellementInfoRequestDto;
import cmr.notep.interfaces.dto.RenouvellementStatutDto;
import cmr.notep.interfaces.modeles.Contrat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/contrats")
public interface ContratApi {

    @GetMapping(path = "/classe/{classeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    Contrat obtenirContratCourantDeLaClasse(@PathVariable("classeId") String classeId);

    @GetMapping(path = "/etablissement/{etablissementId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    Contrat obtenirContratCourantDeLetablissement(@PathVariable("etablissementId") String etablissementId);

    @PostMapping(path = "/classe/{classeId}/prolonger", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    Contrat prolongerContratClasse(@PathVariable("classeId") String classeId, @RequestBody ContratActionDto action);

    @PostMapping(path = "/classe/{classeId}/changer-offre", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    Contrat changerOffreClasse(@PathVariable("classeId") String classeId, @RequestBody ContratActionDto action);

    @PostMapping(path = "/etablissement/{etablissementId}/prolonger", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    Contrat prolongerContratEtablissement(@PathVariable("etablissementId") String etablissementId, @RequestBody ContratActionDto action);

    @PostMapping(path = "/etablissement/{etablissementId}/changer-offre", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    Contrat changerOffreEtablissement(@PathVariable("etablissementId") String etablissementId, @RequestBody ContratActionDto action);

    // Renouvellement sans session (via lien email, voir RenouvellementInfoRequestDto)
    @PostMapping(path = "/renouvellement-info", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    void demanderLienRenouvellement(@RequestBody RenouvellementInfoRequestDto requestDto);

    @GetMapping(path = "/renouvellement/{token}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    RenouvellementStatutDto obtenirStatutRenouvellement(@PathVariable("token") String token);

    @PostMapping(path = "/renouvellement/{token}/prolonger", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    Contrat prolongerParToken(@PathVariable("token") String token, @RequestBody ContratActionDto action);

    @PostMapping(path = "/renouvellement/{token}/changer-offre", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    Contrat changerOffreParToken(@PathVariable("token") String token, @RequestBody ContratActionDto action);

    // Attribution directe par un admin (support), sans passer par le paiement simule.
    // Ecriture reservee ADMIN : voir @PreAuthorize sur l'implementation ContratService (business/impl).
    @PostMapping(path = "/admin/classe/{classeId}/assigner", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    Contrat assignerOffreClasseParAdmin(@PathVariable("classeId") String classeId, @RequestBody ContratActionDto action);

    @PostMapping(path = "/admin/etablissement/{etablissementId}/assigner", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    Contrat assignerOffreEtablissementParAdmin(@PathVariable("etablissementId") String etablissementId, @RequestBody ContratActionDto action);
}
