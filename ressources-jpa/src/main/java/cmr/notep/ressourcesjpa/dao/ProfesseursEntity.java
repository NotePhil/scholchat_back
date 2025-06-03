package cmr.notep.ressourcesjpa.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "professeurs", schema = "ressources")
@PrimaryKeyJoinColumn(name = "professeurs_id")
public class ProfesseursEntity extends UtilisateursEntity {

    @Column(name = "cni_url_front", nullable = false)
    private String cniUrlRecto;

    @Column(name = "cni_url_back", nullable = false)
    private String cniUrlVerso;


    //TODO : vérifier si le matricule est lié à l'établissement uniquement comme dans les collèges
    @Column(name = "matricule_professeur", nullable = false, unique = true)
    private String matriculeProfesseur;

    @OneToMany(mappedBy = "professeur", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CanalEntity> canaux = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "professeur_matiere", schema = "ressources",
            joinColumns = @JoinColumn(name = "professeur_id"),
            inverseJoinColumns = @JoinColumn(name = "matiere_id"))
    private List<MatiereEntity> matieresEnseignees = new ArrayList<>();

    @OneToMany(mappedBy = "createur", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EvenementEntity> evenementsCrees = new ArrayList<>();


}
