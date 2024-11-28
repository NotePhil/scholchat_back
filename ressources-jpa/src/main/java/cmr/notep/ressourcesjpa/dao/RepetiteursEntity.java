package cmr.notep.ressourcesjpa.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entité JPA représentant un répétiteur dans la base de données.
 */
@Getter
@Setter
@Entity
@Table(name = "repetiteurs")
public class RepetiteursEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID auto-incrémenté

    @Column(name = "piece_identite", nullable = false)
    private String pieceIdentite; // Pièce d'identité obligatoire

    @Column(name = "photo", nullable = false)
    private String photo; // Photo obligatoire
}
