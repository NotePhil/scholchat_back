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

    // Plus de collection JPA "enfants" (@ManyToMany sur parent_eleve) : les comptes sont mis à jour par
    // fusion d'une entité reconstruite depuis le modèle (Dozer), où la collection était toujours null —
    // chaque mise à jour du parent (changement de mot de passe, PATCH du profil…) VIDAIT parent_eleve.
    // Les liens se lisent / s'écrivent via ParentEleveRepository / ParentEleveEntity.
}
