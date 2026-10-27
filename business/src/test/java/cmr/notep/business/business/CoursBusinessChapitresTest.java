package cmr.notep.business.business;

import cmr.notep.interfaces.modeles.Chapitre;
import cmr.notep.ressourcesjpa.dao.ChapitreEntity;
import cmr.notep.ressourcesjpa.dao.CoursEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class CoursBusinessChapitresTest {

    private static ChapitreEntity existant(CoursEntity cours, String id, int ordre) {
        ChapitreEntity c = new ChapitreEntity();
        c.setId(id);
        c.setOrdre(ordre);
        c.setTitre("Ancien " + id);
        c.setContenu("<p>ancien</p>");
        c.setCours(cours);
        cours.getChapitres().add(c);
        return c;
    }

    private static Chapitre envoye(String id, String titre, int ordre) {
        Chapitre c = new Chapitre();
        c.setId(id);
        c.setTitre(titre);
        c.setOrdre(ordre);
        c.setContenu("<p>" + titre + "</p>");
        return c;
    }

    @Test
    void chapitresAvecIdMisAJourSurPlaceEtManquantsSupprimes() {
        CoursEntity cours = new CoursEntity();
        ChapitreEntity ch1 = existant(cours, "ch-1", 1);
        existant(cours, "ch-2", 2);
        ChapitreEntity ch3 = existant(cours, "ch-3", 3);

        String contenu = CoursBusiness.fusionnerChapitres(cours, List.of(
                envoye("ch-3", "Trois", 1),
                envoye("ch-1", "Un", 2),
                envoye(null, "Nouveau", 3)));

        assertEquals(3, cours.getChapitres().size());
        assertSame(ch3, cours.getChapitres().stream().filter(c -> "ch-3".equals(c.getId())).findFirst().orElseThrow());
        assertEquals("Trois", ch3.getTitre());
        assertEquals(1, ch3.getOrdre());
        assertEquals("Un", ch1.getTitre());
        assertFalse(cours.getChapitres().stream().anyMatch(c -> "ch-2".equals(c.getId())));
        ChapitreEntity nouveau = cours.getChapitres().stream()
                .filter(c -> "Nouveau".equals(c.getTitre())).findFirst().orElseThrow();
        assertNotNull(nouveau.getId());
        assertSame(cours, nouveau.getCours());
        assertEquals("=== Trois ===\n<p>Trois</p>\n\n=== Un ===\n<p>Un</p>\n\n=== Nouveau ===\n<p>Nouveau</p>\n\n", contenu);
    }

    @Test
    void anciensClientsSansIdReprennentLesChapitresDansLOrdre() {
        CoursEntity cours = new CoursEntity();
        ChapitreEntity ch2 = existant(cours, "ch-2", 2);
        ChapitreEntity ch1 = existant(cours, "ch-1", 1);

        CoursBusiness.fusionnerChapitres(cours, List.of(
                envoye(null, "Premier modifié", 1),
                envoye(null, "Second modifié", 2)));

        assertEquals(2, cours.getChapitres().size());
        assertEquals("Premier modifié", ch1.getTitre());
        assertEquals("Second modifié", ch2.getTitre());
    }
}
