package cmr.notep.business.impl;

import cmr.notep.business.business.OffreBusiness;
import cmr.notep.interfaces.api.OffreApi;
import cmr.notep.interfaces.modeles.Offre;
import cmr.notep.modele.TypeCibleOffre;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class OffreService implements OffreApi {

    private final OffreBusiness offreBusiness;

    @Override
    public List<Offre> listerOffres(TypeCibleOffre cible, boolean toutes) {
        log.info("Listing offres - cible={}, toutes={}", cible, toutes);
        return offreBusiness.listerOffres(cible, toutes);
    }

    @Override
    public Offre obtenirOffreParId(String id) {
        return offreBusiness.obtenirOffreParId(id);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Offre creerOffre(Offre offre) {
        log.info("Création d'une offre (admin): {}", offre.getNom());
        return offreBusiness.creerOffre(offre);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Offre modifierOffre(String id, Offre offre) {
        log.info("Modification de l'offre (admin): {}", id);
        return offreBusiness.modifierOffre(id, offre);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void desactiverOffre(String id) {
        log.info("Désactivation de l'offre (admin): {}", id);
        offreBusiness.desactiverOffre(id);
    }
}
