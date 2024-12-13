package cmr.notep.ressourcesjpa.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "eleves", schema = "ressources")
public class ElevesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
    private String id;

    @Column(name = "nom", nullable = false)
    private String nom;

    @ManyToMany(mappedBy = "elevesEntities")
    private List<ClassesEntity> classes;
}
