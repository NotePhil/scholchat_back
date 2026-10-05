package cmr.notep.interfaces.modeles;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(value = {"messagesEnvoyer", "messagesRecus"}, ignoreUnknown = true)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Professeurs extends Utilisateurs {
    private String cniUrlRecto;
    private String cniUrlVerso;
    private String selfieUrl;
    private String matriculeProfesseur;
    private boolean hasUploaded;
    /**
     * Statut de vérification du profil professeur (DOCUMENTS_MANQUANTS, EN_ATTENTE_VALIDATION, VALIDE,
     * REJETE), en lecture seule : renseigné par le serveur, jamais lu depuis une requête client.
     */
    private String statutVerification;
    /** Motif du dernier refus des pièces (statut REJETE). */
    private String motifRejetVerification;
    @JsonIdentityReference(alwaysAsId = true)
    private List<Classes> moderatedClasses;

    @JsonIgnore
    private boolean isModerator;
}