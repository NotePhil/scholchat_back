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
 *
 * role (rôle le plus fort de l'utilisateur sur la classe, dédupliqué) :
 * - CREATEUR    : l'utilisateur a créé la classe (creatorId), même si un autre la modère désormais
 * - MODERATEUR  : modérateur principal ou co-modérateur (professeur_classes_moderees)
 * - PUBLICATION : droit de publication accordé (peutModerer indique un droit de modération délégué)
 * creatorNom / moderateurNom : noms d'affichage du créateur et du modérateur principal.
 */
@Data
@NoArgsConstructor
public class ClasseAvecDroitDto implements Serializable {
    private Classes classe;
    private boolean peutPublier;
    private boolean peutModerer;
    private boolean estCreateur;
    private String role;
    private String creatorNom;
    private String moderateurNom;

    public static final String ROLE_CREATEUR = "CREATEUR";
    public static final String ROLE_MODERATEUR = "MODERATEUR";
    public static final String ROLE_PUBLICATION = "PUBLICATION";

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
