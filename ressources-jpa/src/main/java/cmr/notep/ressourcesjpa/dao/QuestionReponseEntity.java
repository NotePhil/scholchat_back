package cmr.notep.ressourcesjpa.dao;

import cmr.notep.modele.TypeQuestion;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "questions_reponses", schema = "ressources")
public class QuestionReponseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String intitule;

    @Column(columnDefinition = "TEXT")
    private String reponse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeQuestion typeQuestion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id")
    private ExerciseEntity exercise;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RepondreEntity> reponsesUtilisateurs = new ArrayList<>();}