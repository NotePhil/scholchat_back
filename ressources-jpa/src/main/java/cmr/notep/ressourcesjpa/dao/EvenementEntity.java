package cmr.notep.ressourcesjpa.dao;

import cmr.notep.modele.EtatEvenement;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "evenements", schema = "ressources")
public class EvenementEntity {

    @Id
    
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "titre", nullable = false)
    private String titre;

    @Column(name = "description")
    private String description;

    @Column(name = "lieu")
    private String lieu;

    @Enumerated(EnumType.STRING)
    @Column(name = "etat")
    private EtatEvenement etat;

    @Column(name = "heure_debut", nullable = false)
    private LocalDateTime heureDebut;

    @Column(name = "heure_fin")
    private LocalDateTime heureFin;


    @ElementCollection
    @CollectionTable(name = "evenement_participants", schema = "ressources",
            joinColumns = @JoinColumn(name = "evenement_id"))
    @Column(name = "utilisateur_id")
    private List<String> participantsIds;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "createur_id", nullable = false)
    private UtilisateursEntity createur;

    @OneToMany(mappedBy = "evenement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MediaEntity> medias;
    
    // Nouveaux champs pour supporter la logique du frontend
    @Column(name = "visibility")
    private String visibility; // PUBLIC ou PRIVATE
    
    @ElementCollection
    @CollectionTable(name = "evenement_classes", schema = "ressources",
            joinColumns = @JoinColumn(name = "evenement_id"))
    @Column(name = "classe_id")
    private List<String> selectedClasses; // IDs des classes sélectionnées pour les événements privés
}