package cmr.notep.ressourcesjpa.dao;

import jakarta.persistence.*;
import lombok.*;
import org.dozer.Mapping;

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

    @ManyToOne
    @JoinColumn(name = "etablissement_id")
    @Mapping("etablissement")
    private EtablissementEntity etablissement;

    @ManyToMany
    @JoinTable(name = "classe_parents",
            joinColumns = @JoinColumn(name = "classe_id"),
            inverseJoinColumns = @JoinColumn(name = "parent_id"))
    @Mapping("parents")
    private List<ParentsEntity> parentsEntities;

    @ManyToMany
    @JoinTable(name = "classe_eleves",
            joinColumns = @JoinColumn(name = "classe_id"),
            inverseJoinColumns = @JoinColumn(name = "eleve_id"))
    @Mapping("eleves")
    private List<ElevesEntity> elevesEntities;
}
