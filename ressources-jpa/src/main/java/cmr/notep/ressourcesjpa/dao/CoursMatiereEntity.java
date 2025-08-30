package cmr.notep.ressourcesjpa.dao;

import cmr.notep.interfaces.modeles.CoursMatiereId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "cours_matiere", schema = "ressources")
public class CoursMatiereEntity {

    @EmbeddedId
    private CoursMatiereId id = new CoursMatiereId();

    @ManyToOne
    @MapsId("coursId")
    @JoinColumn(name = "cours_id")
    private CoursEntity cours;

    @ManyToOne
    @MapsId("matiereId")
    @JoinColumn(name = "matiere_id")
    private MatiereEntity matiere;

    @Column(name = "date_ajout")
    private Date dateAjout;

    @Column(name = "ordre_dans_cours")
    private Integer ordreDansCours;

//    @OneToMany(mappedBy = "coursMatiere", cascade = CascadeType.ALL)
//    private List<ChapitreEntity> chapitres = new ArrayList<>();


}