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

    @Column(name = "cni_url_front", nullable = true )
    private String cniUrlRecto;

    @Column(name = "cni_url_back", nullable = true)
    private String cniUrlVerso;

    @Column(name = "selfie_url")
    private String selfieUrl;

    @Column(name = "matricule_professeur", unique = true)
    private String matriculeProfesseur;

    @Column(name = "has_uploaded")
    private Boolean hasUploaded = false;

    @OneToMany(mappedBy = "professeur", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CanalEntity> canaux = new ArrayList<>();

    // Changed fetch type to LAZY to prevent unnecessary loading
    @OneToMany(mappedBy = "moderator", fetch = FetchType.LAZY)
    private List<ClassesEntity> moderatedClasses = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "professeur_matiere", schema = "ressources",
            joinColumns = @JoinColumn(name = "professeur_id"),
            inverseJoinColumns = @JoinColumn(name = "matiere_id"))
    private List<MatiereEntity> matieresEnseignees = new ArrayList<>();

    @OneToMany(mappedBy = "createur", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EvenementEntity> evenementsCrees = new ArrayList<>();


}