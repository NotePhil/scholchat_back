package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.UtilisateursEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UtilisateursRepository extends JpaRepository<UtilisateursEntity, String> {
    Optional<UtilisateursEntity> findByActivationToken(String activationToken);
}
