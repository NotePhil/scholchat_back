package cmr.notep.ressourcesjpa.dao;

import cmr.notep.modele.DroitPublication;
import cmr.notep.modele.EtatClasse;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dozer.Mapping;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "classes", schema = "ressources")
public class ClassesEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "niveau", nullable = false)
    private String niveau;

    @Column(name = "date_creation")
    private Date dateCreation;

    @Column(name = "code_activation")
    private String codeActivation;

    @Enumerated(EnumType.STRING)
    @Column(name = "etat")
    private EtatClasse etat;

    @Enumerated(EnumType.STRING)
    @Column(name = "droit_publication")
    private DroitPublication droitPublication;

    @ManyToOne
    @JoinColumn(name = "etablissement_id")
    @Mapping("etablissement")
    private EtablissementEntity etablissement;

    @ManyToOne
    @JoinColumn(name = "moderator_id")
    @Mapping("moderator")
    private ProfesseursEntity moderator;

    @Column(name = "creator_id")
    private String creatorId;

    @ManyToMany(mappedBy = "classes")
    private List<MessagesEntity> messages;

    @OneToMany(mappedBy = "classe")
    private List<CanalEntity> canaux = new ArrayList<>();

    @OneToMany(mappedBy = "classe")
    private List<HistoActivationEntity> activationHistory = new ArrayList<>();

    @OneToMany(mappedBy = "classe")
    private List<AccederEntity> utilisateursAccedant;

    @Column(name = "acces_majeur")
    private boolean accesMajeur;

    @Column(name = "payment_required")
    private boolean paymentRequired;

    @ManyToMany(mappedBy = "classesDiffusees")
    private List<ExerciseProgrammerEntity> exercisesProgrammes = new ArrayList<>();

    @ManyToMany(mappedBy = "classes")
    private List<EvenementEntity> evenements = new ArrayList<>();
}
