package cmr.notep.interfaces.modeles;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Enfant inscrit par un parent dans une classe à l'aide du code de la classe.
 * <ul>
 *   <li>Entrée : élément de {@code enfants} de POST /utilisateurs (type parent) et corps de
 *       POST /parents/{id}/enfants/inscription : {@code prenom}, {@code nom}, {@code codeClasse}.</li>
 *   <li>Réponse : {@code id} (élève créé), {@code prenom}, {@code nom}, {@code niveau}, {@code classeId},
 *       {@code classeNom}, {@code statut} (EN_ATTENTE à la création).</li>
 * </ul>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnfantInscription implements Serializable {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id;
    public static final String TYPE_MINEUR = "MINEUR";
    public static final String TYPE_MAJEUR = "MAJEUR";

    /** MINEUR (défaut : enfant sans compte, prénom + nom + code) | MAJEUR (élève avec son propre compte, e-mail). */
    private String type;
    /**
     * MAJEUR : e-mail du compte élève existant de l'enfant, avec le(s) code(s) des classes à suivre
     * ({@code codesClasse} et/ou {@code codeClasse}, au moins un) ; prenom / nom ignorés.
     */
    private String email;
    private String prenom;
    private String nom;
    /** Entrée uniquement : code d'activation de la classe. */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String codeClasse;
    /** Entrée uniquement, MAJEUR : codes des classes de l'enfant à suivre (une demande de suivi par classe). */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private java.util.List<String> codesClasse;
    /** Réponse, MAJEUR : classes demandées (statut EN_ATTENTE, classeId, classeNom, demandeSuiviId). */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private java.util.List<EnfantStatut.ClasseStatut> classesSuivi;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String niveau;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String classeId;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String classeNom;
    /** APPROUVEE | EN_ATTENTE | REJETEE (EN_ATTENTE à la création). */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String statut;
    /** MAJEUR : EN_ATTENTE_VALIDATION_PROFESSEUR (demande de suivi à valider par un professeur de l'enfant). */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String lienStatut;

    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean estMajeur() {
        return type != null && TYPE_MAJEUR.equalsIgnoreCase(type.trim());
    }
}
