package cmr.notep.interfaces.modeles;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(value = {"messagesEnvoyer", "messagesRecus"}, ignoreUnknown = true)
public class Parents extends Utilisateurs {
//    List<Classes> classes;

    /**
     * POST /utilisateurs (inscription publique d'un parent) : enfants à inscrire, chacun avec le code de sa
     * classe ({@code [{prenom, nom, codeClasse}]}, au moins un). Réponse : enfants créés (id, classe, statut
     * EN_ATTENTE). Jamais persisté tel quel.
     * Propriété Java nommée différemment de ParentsEntity#enfants (liste JPA parent_eleve) pour que Dozer ne les
     * mappe jamais l'une sur l'autre ; nom JSON : "enfants".
     */
    @JsonProperty("enfants")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<EnfantInscription> enfantsInscription;
}
