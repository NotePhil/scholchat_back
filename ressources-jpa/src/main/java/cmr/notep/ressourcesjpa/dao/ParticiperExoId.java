package cmr.notep.ressourcesjpa.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class ParticiperExoId implements java.io.Serializable { // Rendre la classe publique

    @Column(name = "utilisateur_id")
    private String utilisateurId;

    @Column(name = "exercise_programmer_id")
    private String exerciseProgrammerId;

    public ParticiperExoId() {}

    public ParticiperExoId(String utilisateurId, String exerciseProgrammerId) {
        this.utilisateurId = utilisateurId;
        this.exerciseProgrammerId = exerciseProgrammerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParticiperExoId)) return false;
        ParticiperExoId that = (ParticiperExoId) o;
        return utilisateurId.equals(that.utilisateurId) &&
                exerciseProgrammerId.equals(that.exerciseProgrammerId);
    }

    @Override
    public int hashCode() {
        return 31 * utilisateurId.hashCode() + exerciseProgrammerId.hashCode();
    }
}