package cmr.notep.ressourcesjpa.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "professeurs")
public class ProfesseurEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url_cni", nullable = false)
    private String urlCni; // This field cannot be null

    @Column(name = "url_photo")
    private String urlPhoto;

    @Column(name = "nom_etablissement", nullable = false)
    private String nomEtablissement;

    @Column(name = "nom_classe", nullable = false)
    private String nomClasse;

    @Column(name = "matricule_professeur", nullable = false, unique = true)
    private String matriculeProfesseur;
}
