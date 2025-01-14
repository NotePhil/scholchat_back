package cmr.notep.ressourcesjpa.dao;

import cmr.notep.modele.EtatUtilisateur;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.dozer.Mapping;

import java.util.List;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "utilisateurs", schema = "ressources")
@Inheritance(strategy = InheritanceType.JOINED)
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

    @Column(name = "activation_token", unique = true)
    private String activationToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "etat")
    private EtatUtilisateur etat = EtatUtilisateur.INACTIVE;

    @Column(name = "creation_date")
    private LocalDateTime creationDate = LocalDateTime.now();

    @OneToMany(mappedBy = "expediteurEntity")
    @Mapping("messagesEnvoyer")
    private List<MessagesEntity> messagesEnvoyerEntities;

    @ManyToMany
    @JoinTable(name = "recevoir", schema = "ressources",
            joinColumns = @JoinColumn(name = "utilisateur_id"),
            inverseJoinColumns = @JoinColumn(name = "message_id"))
    @Mapping("messagesRecus")
    private List<MessagesEntity> messagesRecusEntities;

}
