package cmr.notep.ressourcesjpa.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "motifs_rejet_classe", schema = "ressources")
public class MotifRejetClasseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String descriptif;

    @Column(nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();
}