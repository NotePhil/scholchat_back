package cmr.notep.ressourcesjpa.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.dozer.Mapping;
import org.hibernate.annotations.GenericGenerator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "utilisateurs", schema = "ressources")
@Inheritance(strategy = InheritanceType.JOINED)
public class UtilisateursEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
    private String id;

    @Column(name = "nom")
    private String nom;

    @Column(name = "prenom")
    private String prenom;

    @Column(name = "email")
    private String email;

    @Column(name = "passeaccess")
    private String passeAccess;

    @Column(name = "telephone")
    private String telephone;

    @Column(name = "adresse")
    private String adresse;

    @Column(name = "etat")
    private String etat;

    @Column(name = "datecreation")
    private Date dateCreation;

    @Column(name = "datemodification")
    private Date dateModification;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "rattacher", schema = "ressources",
            joinColumns = @JoinColumn(name = "utilisateur_id"),
            inverseJoinColumns = @JoinColumn(name = "rattache_id"))
    @Mapping("utilisateursRatachees")
    private List<UtilisateursEntity> utilisateursRatachees = new ArrayList<>();

    @OneToMany(mappedBy = "expediteurEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @Mapping("messagesEnvoyer")
    private List<MessagesEntity> messagesEnvoyerEntities = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "recevoir", schema = "ressources",
            joinColumns = @JoinColumn(name = "utilisateur_id"),
            inverseJoinColumns = @JoinColumn(name = "message_id"))
    @Mapping("messagesRecus")
    private List<MessagesEntity> messagesRecusEntities = new ArrayList<>();
}
