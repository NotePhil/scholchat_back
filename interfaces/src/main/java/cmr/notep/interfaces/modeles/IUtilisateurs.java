package cmr.notep.interfaces.modeles;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Utilisateurs.class, name = "utilisateur"),
        @JsonSubTypes.Type(value = Professeurs.class, name = "professeur"),
        @JsonSubTypes.Type(value = Eleves.class, name = "eleve"),
        @JsonSubTypes.Type(value = Repetiteurs.class, name = "repetiteur"),
        @JsonSubTypes.Type(value = Parents.class, name = "parent")
})
public interface IUtilisateurs {
}