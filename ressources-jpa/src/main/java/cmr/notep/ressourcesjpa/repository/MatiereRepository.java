package cmr.notep.ressourcesjpa.repository;

import cmr.notep.modele.NomMatiere;
import cmr.notep.ressourcesjpa.dao.MatiereEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MatiereRepository extends JpaRepository<MatiereEntity, String> {
    Optional<MatiereEntity> findByNom(NomMatiere nom);
}
