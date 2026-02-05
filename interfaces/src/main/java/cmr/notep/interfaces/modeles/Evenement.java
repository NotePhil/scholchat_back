package cmr.notep.interfaces.modeles;

import cmr.notep.interfaces.modeles.Media;
import cmr.notep.interfaces.modeles.Interaction;
import cmr.notep.modele.EtatEvenement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Evenement {
    private String id;
    private String titre;
    private String description;
    private String lieu;
    private EtatEvenement etat;
    private LocalDateTime heureDebut;
    private LocalDateTime heureFin;
    // Change from Utilisateurs to String for creator ID
    private String createurId;

    // Change from List<Utilisateurs> to List<String> for participant IDs
    private List<String> participantsIds;

    private List<Media> medias;
    
    private List<Interaction> interactions;
    
    // Nouveaux champs pour supporter la logique du frontend
    private String visibility; // PUBLIC ou PRIVATE
    private List<String> selectedClasses; // IDs des classes sélectionnées pour les événements privés
}