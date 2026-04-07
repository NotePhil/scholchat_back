package cmr.notep.ressourcesjpa.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class ProgressionChapitreId implements Serializable {

    @Column(name = "utilisateur_id")
    private String utilisateurId;

    @Column(name = "chapitre_id")
    private String chapitreId;

    public ProgressionChapitreId() {}

    public ProgressionChapitreId(String utilisateurId, String chapitreId) {
        this.utilisateurId = utilisateurId;
        this.chapitreId = chapitreId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProgressionChapitreId)) return false;
        ProgressionChapitreId that = (ProgressionChapitreId) o;
        return Objects.equals(utilisateurId, that.utilisateurId) && Objects.equals(chapitreId, that.chapitreId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(utilisateurId, chapitreId);
    }
}
