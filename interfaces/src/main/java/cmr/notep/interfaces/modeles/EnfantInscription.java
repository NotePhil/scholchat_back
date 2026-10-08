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
    private String prenom;
    private String nom;
    /** Entrée uniquement : code d'activation de la classe. */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String codeClasse;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String niveau;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String classeId;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String classeNom;
    /** APPROUVEE | EN_ATTENTE | REJETEE (EN_ATTENTE à la création). */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String statut;
}
