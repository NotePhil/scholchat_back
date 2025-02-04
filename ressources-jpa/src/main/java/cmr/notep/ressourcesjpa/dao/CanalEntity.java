package cmr.notep.ressourcesjpa.dao;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "canaux", schema = "ressources")
public class CanalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
    private String id;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "professeur_id", nullable = false)
    private ProfesseursEntity professeur;

    @ManyToOne
    @JoinColumn(name = "classe_id", nullable = false)
    private ClassesEntity classe;
}
