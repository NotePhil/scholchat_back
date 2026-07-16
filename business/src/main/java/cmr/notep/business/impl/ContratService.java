package cmr.notep.business.impl;

import cmr.notep.business.business.ContratBusiness;
import cmr.notep.interfaces.api.ContratApi;
import cmr.notep.interfaces.dto.ContratActionDto;
import cmr.notep.interfaces.dto.RenouvellementInfoRequestDto;
import cmr.notep.interfaces.dto.RenouvellementStatutDto;
import cmr.notep.interfaces.modeles.Contrat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ContratService implements ContratApi {

    private final ContratBusiness contratBusiness;

    @Override
    public Contrat obtenirContratCourantDeLaClasse(String classeId) {
        return contratBusiness.obtenirContratCourantDeLaClasse(classeId);
    }

    @Override
    public Contrat obtenirContratCourantDeLetablissement(String etablissementId) {
        return contratBusiness.obtenirContratCourantDeLetablissement(etablissementId);
    }

    @Override
    public Contrat prolongerContratClasse(String classeId, ContratActionDto action) {
        log.info("Prolongation du contrat de la classe: {}", classeId);
        return contratBusiness.prolongerContratClasse(classeId, action, currentUserId(), currentUserIsAdmin());
    }

    @Override
    public Contrat changerOffreClasse(String classeId, ContratActionDto action) {
        log.info("Changement d'offre pour la classe: {}", classeId);
        return contratBusiness.changerOffreClasse(classeId, action, currentUserId(), currentUserIsAdmin());
    }

    @Override
    public Contrat prolongerContratEtablissement(String etablissementId, ContratActionDto action) {
        log.info("Prolongation du contrat de l'établissement: {}", etablissementId);
        return contratBusiness.prolongerContratEtablissement(etablissementId, action, currentUserId(), currentUserIsAdmin());
    }

    @Override
    public Contrat changerOffreEtablissement(String etablissementId, ContratActionDto action) {
        log.info("Changement d'offre pour l'établissement: {}", etablissementId);
        return contratBusiness.changerOffreEtablissement(etablissementId, action, currentUserId(), currentUserIsAdmin());
    }

    @Override
    public void demanderLienRenouvellement(RenouvellementInfoRequestDto requestDto) {
        contratBusiness.demanderLienRenouvellement(requestDto);
    }

    @Override
    public RenouvellementStatutDto obtenirStatutRenouvellement(String token) {
        return contratBusiness.obtenirStatutRenouvellement(token);
    }

    @Override
    public Contrat prolongerParToken(String token, ContratActionDto action) {
        return contratBusiness.prolongerParToken(token, action);
    }

    @Override
    public Contrat changerOffreParToken(String token, ContratActionDto action) {
        return contratBusiness.changerOffreParToken(token, action);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Contrat assignerOffreClasseParAdmin(String classeId, ContratActionDto action) {
        log.info("Attribution d'offre par admin pour la classe: {}", classeId);
        return contratBusiness.assignerOffreParAdmin(classeId, null, action.getNouvelleOffreId(), action.getPeriodicite(), currentUserId());
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Contrat assignerOffreEtablissementParAdmin(String etablissementId, ContratActionDto action) {
        log.info("Attribution d'offre par admin pour l'établissement: {}", etablissementId);
        return contratBusiness.assignerOffreParAdmin(null, etablissementId, action.getNouvelleOffreId(), action.getPeriodicite(), currentUserId());
    }

    private String currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        return contratBusiness.resolveUserIdByEmail(auth.getName());
    }

    private boolean currentUserIsAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
