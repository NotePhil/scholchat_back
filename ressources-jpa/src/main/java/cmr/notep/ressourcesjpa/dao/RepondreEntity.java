package cmr.notep.ressourcesjpa.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "repondre", schema = "ressources")
public class RepondreEntity {

    @EmbeddedId
    private RepondreId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("utilisateurId")
    @JoinColumn(name = "utilisateur_id")
    private UtilisateursEntity utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("questionId")
    @JoinColumn(name = "question_id")
    private QuestionReponseEntity question;

    @Column(name = "note")
    private String note;

    @Column(name = "appreciation")
    private String appreciation;

    @Column(name = "reponse_utilisateur", columnDefinition = "TEXT")
    private String reponseUtilisateur;

    @Column(name = "date_reponse")
    private LocalDateTime dateReponse = LocalDateTime.now();

    @Column(name = "est_correcte")
    private Boolean estCorrecte;
}