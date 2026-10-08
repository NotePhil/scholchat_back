package cmr.notep.business.business;

import org.junit.jupiter.api.Test;

import static cmr.notep.business.business.SuiviPedagogiqueBusiness.noteSur20;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SuiviPedagogiqueNoteTest {

    @Test
    void notesAvecBaremeRameneesSur20() {
        assertEquals(15.0, noteSur20("15/20"));
        assertEquals(20.0, noteSur20("2/2"));
        assertEquals(15.0, noteSur20(" 7,5 / 10 "));
        assertEquals(10.0, noteSur20("10 / 20"));
        assertEquals(20.0, noteSur20("25/20"), "bornée à 20");
    }

    @Test
    void noteSansBaremeSupposeeSur20() {
        assertEquals(19.0, noteSur20("19"));
        assertEquals(12.5, noteSur20("12,5"));
        assertNull(noteSur20("45"));
    }

    @Test
    void notesIllisibles() {
        assertNull(noteSur20(null));
        assertNull(noteSur20(""));
        assertNull(noteSur20("abc"));
        assertNull(noteSur20("3/0"));
        assertNull(noteSur20("/20"));
    }
}
