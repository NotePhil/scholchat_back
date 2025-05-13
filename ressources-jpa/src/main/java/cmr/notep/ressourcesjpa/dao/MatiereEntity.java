package cmr.notep.ressourcesjpa.dao;

import cmr.notep.modele.NomMatiere;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "matieres", schema = "ressources")
public class MatiereEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "nom", nullable = false, unique = true)
    private NomMatiere nom;

    @ManyToMany(mappedBy = "matieresEnseignees")
    private List<ProfesseursEntity> professeurs;
}