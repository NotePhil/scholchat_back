package cmr.notep.ressourcesjpa.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "messages", schema = "ressources")
public class MessagesEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

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
    private UtilisateursEntity expediteurEntity;


    @ManyToOne
    @JoinColumn(name = "classe_id")
    private ClassesEntity classe;

    @ManyToMany
    @JoinTable(
            name = "recevoir",
            joinColumns = @JoinColumn(name = "message_id"),
            inverseJoinColumns = @JoinColumn(name = "utilisateur_id")
    )
    private List<UtilisateursEntity> destinatairesEntities;
}
