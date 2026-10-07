package cmr.notep.ressourcesjpa.dao;

import cmr.notep.modele.EtatUtilisateur;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.dozer.Mapping;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "utilisateurs", schema = "ressources")
@Inheritance(strategy = InheritanceType.JOINED)
public class UtilisateursEntity {

    @Id
    
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "nom")
    private String nom;

    @Column(name = "prenom")
    private String prenom;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "passeaccess")
    private String passeAccess;

    @Column(name = "telephone")
    private String telephone;

    @Column(name = "adresse")
    private String adresse;

    @Column(name = "activation_token", unique = true)
    private String activationToken;

    /** Jeton de réinitialisation du mot de passe en cours (lien e-mail), usage unique. */
    @Column(name = "reset_password_token", columnDefinition = "TEXT")
    private String resetPasswordToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "etat")
    private EtatUtilisateur etat = EtatUtilisateur.INACTIVE;

    @Column(name = "creation_date")
    private LocalDateTime creationDate = LocalDateTime.now();

    @Column(name = "is_admin")
    private Boolean admin = false;

    /**
     * Mot de passe temporaire (envoyé par e-mail à l'approbation d'une inscription par code de classe) :
     * tant que vrai, seules quelques routes sont accessibles (voir MotDePasseAChangerService) jusqu'au
     * choix d'un nouveau mot de passe via POST /auth/change-password.
     */
    @Column(name = "must_change_password", nullable = false)
    private boolean mustChangePassword = false;

    @OneToMany(mappedBy = "expediteurEntity", fetch = FetchType.EAGER)
    @Mapping("messagesEnvoyer")
    private List<MessagesEntity> messagesEnvoyerEntities;

    @ManyToMany
    @JoinTable(name = "recevoir", schema = "ressources",
            joinColumns = @JoinColumn(name = "utilisateur_id"),
            inverseJoinColumns = @JoinColumn(name = "message_id"))
    @Mapping("messagesRecus")
    private List<MessagesEntity> messagesRecusEntities;

    @OneToMany(mappedBy = "utilisateur")
    private List<AccederEntity> accesClasses;

    @ManyToMany
    @JoinTable(
            name = "classe_eleves",
            schema = "ressources",
            joinColumns = @JoinColumn(name = "eleve_id"),
            inverseJoinColumns = @JoinColumn(name = "classe_id")
    )
    private List<ClassesEntity> classes;

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RepondreEntity> reponsesQuestions = new ArrayList<>();

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParticiperExoEntity> participationsExercices = new ArrayList<>();

    @OneToMany(mappedBy = "gestionnaire", cascade = CascadeType.ALL)
    private List<EtablissementEntity> etablissementsGeres = new ArrayList<>();
}
