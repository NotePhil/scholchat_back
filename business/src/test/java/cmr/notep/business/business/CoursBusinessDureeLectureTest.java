package cmr.notep.business.business;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CoursBusinessDureeLectureTest {

    @Test
    void dureeAbsenteOuNegativeVautZero() {
        assertEquals(0L, CoursBusiness.dureeLectureValide(null));
        assertEquals(0L, CoursBusiness.dureeLectureValide(-5L));
    }

    @Test
    void dureeConserveeSousLePlafond() {
        assertEquals(754L, CoursBusiness.dureeLectureValide(754L));
    }

    @Test
    void dureePlafonneeAQuatreHeures() {
        assertEquals(4 * 3600L, CoursBusiness.dureeLectureValide(99_999L));
    }
}
