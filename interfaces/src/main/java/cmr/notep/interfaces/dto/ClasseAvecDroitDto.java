package cmr.notep.interfaces.dto;

import cmr.notep.interfaces.modeles.Classes;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Wraps a class with the caller's publication rights flags.
 * - peutModerer=true + estCreateur=true  → user created this class (is the original moderator)
 * - peutModerer=true + estCreateur=false → user was assigned as moderator by someone else
 * - peutModerer=false                    → publication rights granted, not moderator
 */
@Data
@NoArgsConstructor
public class ClasseAvecDroitDto implements Serializable {
    private Classes classe;
    private boolean peutPublier;
    private boolean peutModerer;
    private boolean estCreateur;

    public ClasseAvecDroitDto(Classes classe, boolean peutPublier, boolean peutModerer) {
        this.classe = classe;
        this.peutPublier = peutPublier;
        this.peutModerer = peutModerer;
        this.estCreateur = peutModerer; // default: moderator = creator
    }

    public ClasseAvecDroitDto(Classes classe, boolean peutPublier, boolean peutModerer, boolean estCreateur) {
        this.classe = classe;
        this.peutPublier = peutPublier;
        this.peutModerer = peutModerer;
        this.estCreateur = estCreateur;
    }
}
