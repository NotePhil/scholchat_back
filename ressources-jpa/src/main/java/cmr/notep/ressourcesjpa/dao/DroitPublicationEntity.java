package cmr.notep.ressourcesjpa.dao;

import cmr.notep.interfaces.modeles.DroitPublicationId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "droit_publication", schema = "ressources")
@IdClass(DroitPublicationId.class)
public class DroitPublicationEntity {

    @Id
    @Column(name = "utilisateur_id")
    private String utilisateurId;

    @Id
    @Column(name = "classe_id")
    private String classeId;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id", insertable = false, updatable = false)
    private UtilisateursEntity utilisateur;

    @ManyToOne
    @JoinColumn(name = "classe_id", insertable = false, updatable = false)
    private ClassesEntity classe;

    @Column(name = "date_attribution")
    private Date dateAttribution;

    @Column(name = "peut_publier")
    private boolean peutPublier;

    @Column(name = "peut_moderer")
    private boolean peutModerer;
}