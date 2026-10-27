package cmr.notep.business.impl;

import cmr.notep.business.security.CurrentUserService;
import cmr.notep.business.services.LienParentService;
import cmr.notep.interfaces.modeles.DemandeLienParent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Suivi parental d'un élève majeur, par classe (voir {@link LienParentService}) :
 * <ul>
 *   <li>Gestionnaires de la classe / administrateur : GET /classes/{classeId}/demandes-suivi,
 *       GET /classes/{classeId}/suivis-parentaux, POST /demandes-suivi/{id}/approuver,
 *       POST /demandes-suivi/{id}/refuser?motif= (ou corps {"motif": "…"}), DELETE /demandes-suivi/{id} (suivi
 *       accepté), DELETE /classes/{classeId}/suivis/{parentId}/{eleveId} ; GET /demandes-suivi (admin : toutes les
 *       demandes en attente).</li>
 *   <li>Parent : POST /demandes-suivi/{id}/annuler (sa propre demande en attente).</li>
 *   <li>Élève : GET /eleves/{eleveId}/suivi-parental (lecture seule : parents qui le suivent).</li>
 * </ul>
 * L'élève ne décide plus : les anciennes routes /eleves/{id}/demandes-lien-parent/{d}/accepter|refuser renvoient 403.
 */
@RestController
@RequiredArgsConstructor
public class DemandesLienParentController {

    private final LienParentService lienParentService;

    @GetMapping(path = "/classes/{classeId}/demandes-suivi", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DemandeLienParent> demandesDeClasse(@PathVariable String classeId) {
        return lienParentService.demandesEnAttenteDeClasse(classeId);
    }

    /** Administrateur : toutes les demandes en attente (dont celles d'élèves membres d'aucune classe). */
    @GetMapping(path = "/demandes-suivi", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DemandeLienParent> demandesEnAttente() {
        return lienParentService.demandesEnAttente();
    }

    @GetMapping(path = "/classes/{classeId}/suivis-parentaux", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DemandeLienParent> suivisDeClasse(@PathVariable String classeId) {
        return lienParentService.suivisDeClasse(classeId);
    }

    @PostMapping(path = "/demandes-suivi/{demandeId}/approuver", produces = MediaType.APPLICATION_JSON_VALUE)
    public DemandeLienParent approuver(@PathVariable String demandeId) {
        return lienParentService.decider(demandeId, true, null);
    }

    @PostMapping(path = "/demandes-suivi/{demandeId}/refuser", produces = MediaType.APPLICATION_JSON_VALUE)
    public DemandeLienParent refuser(@PathVariable String demandeId,
                                     @RequestParam(required = false) String motif,
                                     @RequestBody(required = false) Map<String, Object> corps) {
        String m = motif;
        if ((m == null || m.isBlank()) && corps != null && corps.get("motif") != null) m = String.valueOf(corps.get("motif"));
        return lienParentService.decider(demandeId, false, m);
    }

    /** Retire un suivi accepté (gestionnaire de sa classe ou admin) ; dernier suivi retiré → lien parent supprimé. */
    @DeleteMapping(path = "/demandes-suivi/{demandeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retirer(@PathVariable String demandeId) {
        lienParentService.retirerSuivi(demandeId);
    }

    @DeleteMapping(path = "/classes/{classeId}/suivis/{parentId}/{eleveId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retirerDeClasse(@PathVariable String classeId, @PathVariable String parentId, @PathVariable String eleveId) {
        lienParentService.retirerSuiviDeClasse(classeId, parentId, eleveId);
    }

    @PostMapping(path = "/demandes-suivi/{demandeId}/annuler", produces = MediaType.APPLICATION_JSON_VALUE)
    public DemandeLienParent annuler(@PathVariable String demandeId) {
        return lienParentService.annulerParParent(demandeId);
    }

    @GetMapping(path = "/eleves/{eleveId}/suivi-parental", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DemandeLienParent> suiviParental(@PathVariable String eleveId) {
        return lienParentService.suiviParentalDeEleve(eleveId);
    }

    /** Ancien flux (l'élève décidait) : supprimé. */
    @PostMapping(path = {"/eleves/{eleveId}/demandes-lien-parent/{demandeId}/accepter",
            "/eleves/{eleveId}/demandes-lien-parent/{demandeId}/refuser"})
    public void ancienneReponseEleve(@PathVariable String eleveId, @PathVariable String demandeId) {
        throw CurrentUserService.forbidden("Les demandes de suivi parental sont validées par le professeur de l'élève, "
                + "pas par l'élève.");
    }
}
