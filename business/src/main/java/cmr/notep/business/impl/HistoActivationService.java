package cmr.notep.business.impl;

import cmr.notep.business.business.HistoActivationBusiness;
import cmr.notep.interfaces.api.HistoActivationApi;
import cmr.notep.interfaces.modeles.HistoActivation;
import cmr.notep.modele.EtatClasse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class HistoActivationService implements HistoActivationApi {
    private final HistoActivationBusiness histoActivationBusiness;

    @Override
    public HistoActivation creerEntreeActivation(@NonNull HistoActivation histoActivation) {
        log.info("Création d'une nouvelle entrée d'activation pour la classe: {}", histoActivation.getClasseId());
        return histoActivationBusiness.creerEntreeActivation(histoActivation);
    }

    @Override
    public HistoActivation desactiverEntree(@NonNull String id, @NonNull String motif) {
        log.info("Désactivation de l'entrée d'activation avec l'ID: {}", id);
        return histoActivationBusiness.desactiverEntree(id, motif);
    }

    @Override
    public List<HistoActivation> obtenirHistoriqueParClasse(@NonNull String classeId) {
        log.info("Récupération de l'historique d'activation pour la classe: {}", classeId);
        return histoActivationBusiness.obtenirHistoriqueParClasse(classeId);
    }

    @Override
    public List<HistoActivation> obtenirHistoriqueParUtilisateur(@NonNull String utilisateurId) {
        log.info("Récupération de l'historique d'activation pour l'utilisateur: {}", utilisateurId);
        return histoActivationBusiness.obtenirHistoriqueParUtilisateur(utilisateurId);
    }

    @Override
    public List<HistoActivation> obtenirActivationsActives() {
        log.info("Récupération des activations actives");
        return histoActivationBusiness.obtenirActivationsActives();
    }

    @Override
    public List<HistoActivation> obtenirParEtatClasse(EtatClasse etatClasse) {
        log.info("Récupération des activations par état de classe: {}", etatClasse);
        return histoActivationBusiness.obtenirParEtatClasse(etatClasse);
    }
}