package cmr.notep.ressourcesjpa.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "gestionnaires", schema = "ressources")
@PrimaryKeyJoinColumn(name = "gestionnaires_id")
public class GestionnairesEntity extends UtilisateursEntity {

    @OneToMany(mappedBy = "gestionnaire", cascade = CascadeType.ALL)
    private List<EtablissementEntity> etablissementsGeres = new ArrayList<>();
}
