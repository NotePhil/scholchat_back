package cmr.notep.ressourcesjpa.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.dozer.Mapping;

import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "messages", schema = "ressources")
public class MessagesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
    private String id;

    @Column(name = "objet")
    private String objet;

    @Column(name = "contenu")
    private String contenu;

    @Column(name = "datecreation")
    private String dateCreation;

    @Column(name = "datemodification")
    private String dateModification;

    @Column(name = "etat")
    private String etat;

    @ManyToOne
    @JoinColumn(name = "expediteur_id")
    @Mapping("expediteur")
    private UtilisateursEntity expediteurEntity;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "recevoir",
            joinColumns = @JoinColumn(name = "message_id"),
            inverseJoinColumns = @JoinColumn(name = "utilisateur_id")
    )
    @Mapping("destinataires")
    private List<UtilisateursEntity> destinatairesEntities;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "message_classes",
            schema = "ressources",
            joinColumns = @JoinColumn(name = "message_id"),
            inverseJoinColumns = @JoinColumn(name = "classe_id")
    )
    private List<ClassesEntity> classes;
}