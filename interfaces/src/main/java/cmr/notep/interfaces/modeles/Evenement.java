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
    /** Date de publication (création du post, UTC) — c'est elle qu'affiche l'en-tête de la carte. */
    private LocalDateTime datePublication;
    /**
     * Statut calculé à la lecture à partir des dates (instants UTC du serveur) : PLANIFIE avant le
     * début, EN_COURS entre début et fin, PASSE après la fin (sans fin : après le jour du début) ;
     * ANNULE reste ANNULE. Ignoré en écriture ; {@link #etat} reste la valeur stockée.
     */
    private String statut;
    // Change from Utilisateurs to String for creator ID
    private String createurId;
    private String createurNom;
    private String createurPrenom;
    private String createurRole;

    // Change from List<Utilisateurs> to List<String> for participant IDs
    private List<String> participantsIds;

    private List<Media> medias;
    
    private List<Interaction> interactions;
    
    // Nouveaux champs pour supporter la logique du frontend
    private String visibility; // PUBLIC ou PRIVATE
    private List<String> classesIds; // IDs des classes concernées par l'événement
}