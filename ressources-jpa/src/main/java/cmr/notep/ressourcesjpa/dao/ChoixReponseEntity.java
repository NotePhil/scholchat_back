package cmr.notep.ressourcesjpa.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "choix_reponses", schema = "ressources")
public class ChoixReponseEntity {
    @Id
    private String id;

    @Column(nullable = false)
    private String texte;

    @Column(nullable = false)
    private Boolean estCorrect = false;

    @Column(name = "ordre_affichage")
    private Integer ordreAffichage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private QuestionReponseEntity question;
}
