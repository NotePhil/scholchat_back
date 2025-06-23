package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.AccederEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface AccederRepository extends JpaRepository<AccederEntity, Long> {
    List<AccederEntity> findByClasseId(String classeId);

    List<AccederEntity> findByUtilisateurId(String utilisateurId);

    boolean existsByUtilisateurIdAndClasseId(String utilisateurId, String classeId);

    Optional<AccederEntity> findByUtilisateurIdAndClasseId(String utilisateurId, String classeId);
}