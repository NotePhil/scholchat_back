package cmr.notep.business.business;

import cmr.notep.business.config.time.ClientTimeZone;
import cmr.notep.business.config.time.ServerDateTimes;
import cmr.notep.interfaces.modeles.Evenement;
import cmr.notep.modele.EtatEvenement;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;

/**
 * Statut d'un événement calculé à partir de ses dates (instants UTC stockés par le serveur), et non
 * de l'état stocké (toujours PLANIFIE à la création, jamais mis à jour ensuite) :
 * <ul>
 *   <li>{@code PLANIFIE} avant le début ;</li>
 *   <li>{@code EN_COURS} entre le début et la fin ;</li>
 *   <li>{@code PASSE} après la fin — sans fin : après le jour du début (dans le fuseau de l'appelant) ;</li>
 *   <li>{@code ANNULE} reste {@code ANNULE}.</li>
 * </ul>
 */
public final class EvenementStatut {

    public static final String PLANIFIE = "PLANIFIE";
    public static final String EN_COURS = "EN_COURS";
    public static final String PASSE = "PASSE";

    private EvenementStatut() {}

    public static String calculer(EtatEvenement etat, LocalDateTime debutUtc, LocalDateTime finUtc,
                                  LocalDateTime maintenantUtc, ZoneId zone) {
        if (etat == EtatEvenement.ANNULE) return EtatEvenement.ANNULE.name();
        if (debutUtc == null) return etat != null ? etat.name() : PLANIFIE;
        if (maintenantUtc.isBefore(debutUtc)) return PLANIFIE;
        if (finUtc != null && !finUtc.isBefore(debutUtc)) {
            return maintenantUtc.isAfter(finUtc) ? PASSE : EN_COURS;
        }
        // Pas de fin : en cours jusqu'à la fin du jour du début (jour vu par l'utilisateur).
        ZoneId z = zone != null ? zone : java.time.ZoneOffset.UTC;
        return ServerDateTimes.inZone(maintenantUtc, z).toLocalDate()
                .isAfter(ServerDateTimes.inZone(debutUtc, z).toLocalDate()) ? PASSE : EN_COURS;
    }

    /** Renseigne {@link Evenement#setStatut} (et rien d'autre) ; renvoie l'événement. */
    public static Evenement appliquer(Evenement e) {
        if (e != null) {
            e.setStatut(calculer(e.getEtat(), e.getHeureDebut(), e.getHeureFin(),
                    ServerDateTimes.nowUtc(), ClientTimeZone.current()));
        }
        return e;
    }

    public static <C extends Collection<Evenement>> C appliquer(C evenements) {
        if (evenements != null) evenements.forEach(EvenementStatut::appliquer);
        return evenements;
    }
}
