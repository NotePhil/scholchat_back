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

    @Column(name = "selfie_url")
    private String selfieUrl;

    @Column(name = "matricule_professeur", unique = true)
    private String matriculeProfesseur;

    @OneToMany(mappedBy = "professeur", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CanalEntity> canaux = new ArrayList<>();

    @OneToMany(mappedBy = "moderator")
    private List<ClassesEntity> moderatedClasses = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "professeur_matiere", schema = "ressources",
            joinColumns = @JoinColumn(name = "professeur_id"),
            inverseJoinColumns = @JoinColumn(name = "matiere_id"))
    private List<MatiereEntity> matieresEnseignees = new ArrayList<>();

    @OneToMany(mappedBy = "createur", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EvenementEntity> evenementsCrees = new ArrayList<>();
}