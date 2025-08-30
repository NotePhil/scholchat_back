package cmr.notep.ressourcesjpa.dao;

import cmr.notep.interfaces.modeles.AccederId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "acceder", schema = "ressources")
@IdClass(AccederId.class)
public class AccederEntity {

    @Id
    @Column(name = "utilisateur_id")
    private String utilisateurId;

    @Id
    @Column(name = "classe_id")
    private String classeId;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id", insertable = false, updatable = false)
    private UtilisateursEntity utilisateur;

    @ManyToOne
    @JoinColumn(name = "classe_id", insertable = false, updatable = false)
    private ClassesEntity classe;

    @Column(name = "date_acces")
    private Date dateAcces;
}
