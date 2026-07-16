package cmr.notep.business.business;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Suppression definitive (purge) d'une classe ou d'un etablissement dont l'offre est expiree
 * depuis plus longtemps que le delai configure par l'admin sur l'offre (voir
 * OffreEntity.delaiSuppressionMinutes et ContratExpirationJob). Supprime explicitement chaque
 * table dependante (dans l'ordre des cles etrangeres) plutot que de compter sur un ON DELETE
 * CASCADE en base, pour garder un controle total et visible sur ce qui est detruit.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClasseSuppressionBusiness {

    private static final String[] TABLES_LIEES_A_LA_CLASSE = {
            "acceder", "canaux", "classe_eleves", "classe_matieres", "classe_parents",
            "contrats", "cours_programmer", "cours_programmer_classes", "demandes_acces",
            "droit_publication", "evenement_classes", "exercise_programmer_classes",
            "histo_activation", "message_classes", "professeur_classes_moderees",
    };

    private final EntityManager entityManager;

    @Transactional
    public void supprimerClasseDefinitivement(String classeId) {
        entityManager.createNativeQuery(
                        "DELETE FROM ressources.cours_programmer_participants WHERE cours_programmer_id IN " +
                                "(SELECT id FROM ressources.cours_programmer WHERE classe_id = :classeId)")
                .setParameter("classeId", classeId)
                .executeUpdate();

        for (String table : TABLES_LIEES_A_LA_CLASSE) {
            entityManager.createNativeQuery("DELETE FROM ressources." + table + " WHERE classe_id = :classeId")
                    .setParameter("classeId", classeId)
                    .executeUpdate();
        }

        entityManager.createNativeQuery("DELETE FROM ressources.classes WHERE id = :classeId")
                .setParameter("classeId", classeId)
                .executeUpdate();

        log.info("Classe {} supprimée définitivement (offre non renouvelée dans le délai configuré)", classeId);
    }

    @Transactional
    public void supprimerEtablissementDefinitivement(String etablissementId) {
        @SuppressWarnings("unchecked")
        List<String> classeIds = entityManager.createNativeQuery(
                        "SELECT id FROM ressources.classes WHERE etablissement_id = :etablissementId")
                .setParameter("etablissementId", etablissementId)
                .getResultList();

        for (String classeId : classeIds) {
            supprimerClasseDefinitivement(classeId);
        }

        entityManager.createNativeQuery("DELETE FROM ressources.contrats WHERE etablissement_id = :etablissementId")
                .setParameter("etablissementId", etablissementId)
                .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM ressources.etablissements WHERE id = :etablissementId")
                .setParameter("etablissementId", etablissementId)
                .executeUpdate();

        log.info("Établissement {} supprimé définitivement ({} classe(s) emportée(s), offre non renouvelée dans le délai)",
                etablissementId, classeIds.size());
    }
}
