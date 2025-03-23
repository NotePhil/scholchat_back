package cmr.notep.business.impl;

import cmr.notep.business.business.EvenementBusiness;
import cmr.notep.interfaces.api.EvenementApi;
import cmr.notep.interfaces.modeles.Evenement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class EvenementService implements EvenementApi {

    private final EvenementBusiness evenementBusiness;

    @Override
    public Evenement creerEvenement(@NonNull Evenement evenement) {
        log.info("Tentative de création d'un nouvel événement: {}", evenement);
        Evenement nouvelEvenement = evenementBusiness.creerEvenement(evenement);
        log.info("Événement créé avec succès: {}", nouvelEvenement.getId());
        return nouvelEvenement;
    }

    @Override
    public Evenement modifierEvenement(@NonNull String id, @NonNull Evenement evenementModifie) {
        log.info("Tentative de modification de l'événement avec l'ID: {}", id);
        Evenement evenementMAJ = evenementBusiness.modifierEvenement(id, evenementModifie);
        log.info("Événement modifié avec succès: {}", evenementMAJ.getId());
        return evenementMAJ;
    }

    @Override
    public void supprimerEvenement(@NonNull String id) {
        log.info("Tentative de suppression de l'événement avec l'ID: {}", id);
        evenementBusiness.supprimerEvenement(id);
        log.info("Événement supprimé avec succès: {}", id);
    }

    @Override
    public Evenement obtenirEvenementParId(@NonNull String id) {
        log.info("Récupération de l'événement avec l'ID: {}", id);
        return evenementBusiness.obtenirEvenementParId(id);
    }

    @Override
    public List<Evenement> obtenirEvenementsParCanal(@NonNull String canalId) {
        log.info("Récupération des événements pour le canal avec l'ID: {}", canalId);
        List<Evenement> evenements = evenementBusiness.obtenirEvenementsParCanal(canalId);
        log.info("Récupération de {} événements pour le canal {}", evenements.size(), canalId);
        return evenements;
    }
}