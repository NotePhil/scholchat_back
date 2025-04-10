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
    //TODO : supprimer ce champ et se mettre en conformité avec le diagramme de classe
    @Column(name = "nom_etablissement")
    private String nomEtablissement;
    //TODO : vérifier si le matricule est lié à l'établissement uniquement comme dans les collèges
    @Column(name = "matricule_professeur", nullable = false, unique = true)
    private String matriculeProfesseur;
    @OneToMany(mappedBy = "professeur", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CanalEntity> canaux = new ArrayList<>();
}
