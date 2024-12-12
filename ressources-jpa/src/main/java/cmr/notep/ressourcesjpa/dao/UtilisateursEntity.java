package cmr.notep.ressourcesjpa.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.dozer.Mapping;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "utilisateurs", schema = "ressources")
@Inheritance(strategy = InheritanceType.JOINED) // JOINED inheritance strategy
public class UtilisateursEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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

    @OneToMany(mappedBy = "expediteurEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @Mapping("messagesEnvoyer")
    private List<MessagesEntity> messagesEnvoyerEntities = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "recevoir",
            schema = "ressources",
            joinColumns = @JoinColumn(name = "utilisateur_id"),
            inverseJoinColumns = @JoinColumn(name = "message_id")
    )
    @Mapping("messagesRecus")
    private List<MessagesEntity> messagesRecusEntities = new ArrayList<>();
}
