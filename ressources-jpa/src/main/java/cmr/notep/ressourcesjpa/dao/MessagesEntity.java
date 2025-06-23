package cmr.notep.ressourcesjpa.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "messages", schema = "ressources")
public class MessagesEntity {

    @Id
    @Column(name = "id")
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

    @ManyToMany
    @JoinTable(
            name = "message_classe_ids",
            joinColumns = @JoinColumn(name = "message_id"),
            inverseJoinColumns = @JoinColumn(name = "classe_id")
    )
    private List<ClassesEntity> classeIds;

    @ManyToMany
    @JoinTable(
            name = "recevoir",
            joinColumns = @JoinColumn(name = "message_id"),
            inverseJoinColumns = @JoinColumn(name = "utilisateur_id")
    )
    private List<UtilisateursEntity> destinatairesEntities;
}
