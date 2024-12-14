package cmr.notep.ressourcesjpa.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "profil_eleves", schema = "ressources")
@PrimaryKeyJoinColumn(name = "profil_eleves_id")
public class ProfilElevesEntity extends UtilisateursEntity {
    // Additional fields can be added as needed
}
