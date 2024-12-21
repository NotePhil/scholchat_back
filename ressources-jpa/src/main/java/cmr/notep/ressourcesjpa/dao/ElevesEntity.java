package cmr.notep.ressourcesjpa.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.dozer.Mapping;

import java.util.List;

@Setter
@Getter
@Entity
@PrimaryKeyJoinColumn(name = "eleves_id")
@Table(name = "eleves", schema = "ressources")
public class ElevesEntity extends UtilisateursEntity {

    @Column(name = "niveau", nullable = false)
    private String niveau;

    @ManyToMany(mappedBy = "elevesEntities")
    @Mapping("classes")
    private List<ClassesEntity> classesEntities;
}
