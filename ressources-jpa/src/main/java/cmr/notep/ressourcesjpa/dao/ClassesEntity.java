package cmr.notep.ressourcesjpa.dao;

import cmr.notep.modele.DroitPublication;
import cmr.notep.modele.EtatClasse;
import jakarta.persistence.*;
import lombok.*;
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
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
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



    @OneToMany(mappedBy = "classe")
    private List<CanalEntity> canaux = new ArrayList<>();

    @OneToMany(mappedBy = "classe")
    private List<HistoActivationEntity> activationHistory = new ArrayList<>();


    @OneToMany(mappedBy = "classe")
    private List<AccederEntity> utilisateursAccedant;
}