package cmr.notep.ressourcesjpa.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_roles", schema = "ressources",
       uniqueConstraints = @UniqueConstraint(columnNames = {"utilisateur_id", "role_type"}))
public class UserRoleEntity {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "utilisateur_id", nullable = false)
    private String utilisateurId;

    @Column(name = "role_type", nullable = false, length = 50)
    private String roleType;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "date_attribution")
    private LocalDateTime dateAttribution = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", insertable = false, updatable = false)
    private UtilisateursEntity utilisateur;
}
