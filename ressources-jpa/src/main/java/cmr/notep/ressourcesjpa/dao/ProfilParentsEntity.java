package cmr.notep.ressourcesjpa.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "parents", schema = "ressources")
@PrimaryKeyJoinColumn(name = "parents_id")
public class ProfilParentsEntity extends UtilisateursEntity {

    @Column(name = "cni_url_front")
    private String cniUrlFront;

    @Column(name = "cni_url_back")
    private String cniUrlBack;

    @Column(name = "full_pic_url")
    private String fullPicUrl;
}