
package cmr.notep.business.impl;

import cmr.notep.business.business.EvenementBusiness;
import cmr.notep.interfaces.api.EvenementApi;
import cmr.notep.interfaces.modeles.Evenement;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class EvenementService implements EvenementApi {

    private final EvenementBusiness evenementBusiness;

    public EvenementService(EvenementBusiness evenementBusiness) {
        this.evenementBusiness = evenementBusiness;
    }

    @Override
    public Evenement creerEvenement(@NonNull Evenement evenement) {
        log.info("Création d'un nouvel événement: {}", evenement.getTitre());
        return evenementBusiness.creerEvenement(evenement);
    }
    @Override
    public List<Evenement> obtenirTousEvenements() {
        log.info("Récupération de tous les événements");
        return evenementBusiness.obtenirTousEvenements();
    }
    @Override
    public Evenement mettreAJourEvenement(@NonNull String id, @NonNull Evenement evenement) {
        log.info("Mise à jour de l'événement avec ID: {}", id);
        return evenementBusiness.mettreAJourEvenement(id, evenement);
    }

    @Override
    public void supprimerEvenement(@NonNull String id) {
        log.info("Suppression de l'événement avec ID: {}", id);
        evenementBusiness.supprimerEvenement(id);
    }

    @Override
    public Evenement obtenirEvenementParId(@NonNull String id) {
        log.info("Récupération de l'événement avec ID: {}", id);
        return evenementBusiness.obtenirEvenementParId(id);
    }

    @Override
    public List<Evenement> obtenirEvenementsParProfesseur(@NonNull String professeurId) {
        log.info("Récupération des événements pour le professeur avec ID: {}", professeurId);
        return evenementBusiness.obtenirEvenementsParProfesseur(professeurId);
    }
}
