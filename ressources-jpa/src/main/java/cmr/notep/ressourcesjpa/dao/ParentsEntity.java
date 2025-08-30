package cmr.notep.ressourcesjpa.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.dozer.Mapping;

import java.util.List;

@Setter
@Getter
@Entity
@PrimaryKeyJoinColumn(name = "parents_id")
@Table(name = "parents", schema = "ressources")
public class ParentsEntity extends UtilisateursEntity {
//    @ManyToMany(mappedBy = "parentsEntities")
//    @Mapping("classes")
//    private List<ClassesEntity> classesEntities;

    @ManyToMany
    @JoinTable(
            name = "parent_eleve",
            schema = "ressources",
            joinColumns = @JoinColumn(name = "parent_id"),
            inverseJoinColumns = @JoinColumn(name = "eleve_id")
    )
    private List<ElevesEntity> enfants;
}
