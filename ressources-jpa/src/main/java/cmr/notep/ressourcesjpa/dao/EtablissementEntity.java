package cmr.notep.ressourcesjpa.dao;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "etablissements", schema = "ressources")
public class EtablissementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
    private String id;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "localisation")
    private String localisation;

    @Column(name = "pays")
    private String pays;

    @Column(name = "email")
    private String email;

    @Column(name = "telephone")
    private String telephone;

    @Column(name = "option_envoi_mail_new_classe")
    private boolean optionEnvoiMailNewClasse;

    @Column(name = "option_token_general")
    private boolean optionTokenGeneral;

    @Column(name = "code_unique", unique = true)
    private String codeUnique;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gestionnaire_id")
    private UtilisateursEntity gestionnaire;

    @OneToMany(mappedBy = "etablissement", cascade = CascadeType.ALL)
    private List<ClassesEntity> classes = new ArrayList<>();
}