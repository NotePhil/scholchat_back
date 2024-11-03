package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.UtilisateursEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UtilisateursRepository extends JpaRepository<UtilisateursEntity, String> {
}
