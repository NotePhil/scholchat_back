package cmr.notep.business.impl;

import cmr.notep.business.business.InteractionBusiness;
import cmr.notep.interfaces.api.InteractionApi;
import cmr.notep.interfaces.modeles.Interaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class InteractionService implements InteractionApi {

    private final InteractionBusiness interactionBusiness;

    @Override
    public Interaction creerInteraction(@NonNull Interaction interaction) {
        log.info("Tentative de création d'une nouvelle interaction: {}", interaction);
        Interaction nouvelleInteraction = interactionBusiness.creerInteraction(interaction);
        log.info("Interaction créée avec succès: {}", nouvelleInteraction.getId());
        return nouvelleInteraction;
    }

    @Override
    public Interaction modifierInteraction(@NonNull String id, @NonNull Interaction interactionModifie) {
        log.info("Tentative de modification de l'interaction avec l'ID: {}", id);
        Interaction interactionMAJ = interactionBusiness.modifierInteraction(id, interactionModifie);
        log.info("Interaction modifiée avec succès: {}", interactionMAJ.getId());
        return interactionMAJ;
    }

    @Override
    public void supprimerInteraction(@NonNull String id) {
        log.info("Tentative de suppression de l'interaction avec l'ID: {}", id);
        interactionBusiness.supprimerInteraction(id);
        log.info("Interaction supprimée avec succès: {}", id);
    }

    @Override
    public Interaction obtenirInteractionParId(@NonNull String id) {
        log.info("Récupération de l'interaction avec l'ID: {}", id);
        return interactionBusiness.obtenirInteractionParId(id);
    }

    @Override
    public List<Interaction> obtenirInteractionsParEvenement(@NonNull String evenementId) {
        log.info("Récupération des interactions pour l'événement avec l'ID: {}", evenementId);
        List<Interaction> interactions = interactionBusiness.obtenirInteractionsParEvenement(evenementId);
        log.info("Récupération de {} interactions pour l'événement {}", interactions.size(), evenementId);
        return interactions;
    }
}