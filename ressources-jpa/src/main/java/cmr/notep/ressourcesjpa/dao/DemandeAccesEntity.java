package cmr.notep.ressourcesjpa.dao;

import cmr.notep.modele.EtatDemandeAcces;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "demandes_acces", schema = "ressources")
public class DemandeAccesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private UtilisateursEntity utilisateur;

    @ManyToOne
    @JoinColumn(name = "classe_id", nullable = false)
    private ClassesEntity classe;

    @Column(nullable = false)
    private String codeActivation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EtatDemandeAcces etat = EtatDemandeAcces.EN_ATTENTE;

    @Column(nullable = false)
    private Date dateDemande = new Date();

    private Date dateTraitement;

    private String motifRejet;
}