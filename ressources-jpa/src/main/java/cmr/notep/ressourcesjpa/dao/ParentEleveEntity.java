package cmr.notep.ressourcesjpa.dao;

import cmr.notep.interfaces.modeles.ParentEleveId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "parent_eleve", schema = "ressources")
@IdClass(ParentEleveId.class)
public class ParentEleveEntity {

    @Id
    @Column(name = "parent_id")
    private String parentId;

    @Id
    @Column(name = "eleve_id")
    private String eleveId;

    @ManyToOne
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private ParentsEntity parent;

    @ManyToOne
    @JoinColumn(name = "eleve_id", insertable = false, updatable = false)
    private ElevesEntity eleve;
}