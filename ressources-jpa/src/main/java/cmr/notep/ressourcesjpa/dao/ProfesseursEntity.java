package cmr.notep.ressourcesjpa.dao;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@DiscriminatorValue("PROFESSEUR") // Assigns "PROFESSEUR" as the discriminator value
public class ProfesseursEntity extends UtilisateursEntity {

    @Column(name = "cni_url_front", nullable = false)
    private String cniUrlFront;

    @Column(name = "cni_url_back", nullable = false)
    private String cniUrlBack;

    @Column(name = "nom_etablissement", nullable = false)
    private String nomEtablissement;

    @Column(name = "nom_classe", nullable = false)
    private String nomClasse;

    @Column(name = "matricule_professeur", nullable = false, unique = true)
    private String matriculeProfesseur;
}
