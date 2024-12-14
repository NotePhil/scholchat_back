package cmr.notep.ressourcesjpa.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "repetiteurs", schema = "ressources")
@PrimaryKeyJoinColumn(name = "repetiteurs_id")
public class RepetiteursEntity extends UtilisateursEntity {

    @Column(name = "cni_url_front", nullable = false)
    private String cniUrlFront;

    @Column(name = "cni_url_back", nullable = false)
    private String cniUrlBack;

    @Column(name = "photo_full_picture", nullable = false)
    private String fullPicUrl;

    @Column(name = "nom_classe", nullable = false)
    private String nomClasse;
}