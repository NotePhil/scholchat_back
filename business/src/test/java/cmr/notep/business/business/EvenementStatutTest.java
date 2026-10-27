package cmr.notep.business.business;

import cmr.notep.modele.EtatEvenement;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EvenementStatutTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 26, 12, 0);

    private static String statut(EtatEvenement etat, LocalDateTime debut, LocalDateTime fin) {
        return EvenementStatut.calculer(etat, debut, fin, NOW, ZoneOffset.UTC);
    }

    @Test
    void planifieAvantLeDebut() {
        assertEquals("PLANIFIE", statut(EtatEvenement.PLANIFIE, NOW.plusHours(1), NOW.plusHours(2)));
    }

    @Test
    void enCoursEntreDebutEtFin() {
        assertEquals("EN_COURS", statut(EtatEvenement.PLANIFIE, NOW.minusHours(1), NOW.plusHours(1)));
    }

    @Test
    void passeApresLaFin_memeSiEtatStockePlanifie() {
        assertEquals("PASSE", statut(EtatEvenement.PLANIFIE, NOW.minusDays(2), NOW.minusDays(1)));
    }

    @Test
    void sansFin_enCoursLeJourDuDebutPuisPasse() {
        assertEquals("EN_COURS", statut(EtatEvenement.PLANIFIE, NOW.minusHours(3), null));
        assertEquals("PASSE", statut(EtatEvenement.PLANIFIE, NOW.minusDays(1), null));
    }

    @Test
    void sansFin_jourVuDansLeFuseauDeLAppelant() {
        // 23:30 UTC le 25 = 00:30 le 26 à Douala (UTC+1) : même jour que "maintenant" (12:00 UTC le 26).
        LocalDateTime debut = LocalDateTime.of(2026, 10, 25, 23, 30);
        assertEquals("PASSE", EvenementStatut.calculer(EtatEvenement.PLANIFIE, debut, null, NOW, ZoneOffset.UTC));
        assertEquals("EN_COURS", EvenementStatut.calculer(EtatEvenement.PLANIFIE, debut, null, NOW, ZoneId.of("Africa/Douala")));
    }

    @Test
    void annuleResteAnnule() {
        assertEquals("ANNULE", statut(EtatEvenement.ANNULE, NOW.minusHours(1), NOW.plusHours(1)));
    }
}
