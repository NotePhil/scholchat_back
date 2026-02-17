package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.AccederEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface AccederRepository extends JpaRepository<AccederEntity, Long> {
    List<AccederEntity> findByClasseId(String classeId);
    @Query("SELECT a FROM AccederEntity a JOIN FETCH a.utilisateur WHERE a.classeId IN :classeIds")
    List<AccederEntity> findByClasseIdIn(@Param("classeIds") List<String> classeIds);
    List<AccederEntity> findByUtilisateurId(String utilisateurId);

    boolean existsByUtilisateurIdAndClasseId(String utilisateurId, String classeId);

    Optional<AccederEntity> findByUtilisateurIdAndClasseId(String utilisateurId, String classeId);

    @Query("SELECT a.utilisateurId FROM AccederEntity a WHERE a.classeId = :classeId")
    List<String> findUserIdsByClasseId(@Param("classeId") String classeId);

    @Query("SELECT c.moderator.id FROM ClassesEntity c WHERE c.id = :classeId AND c.moderator IS NOT NULL")
    List<String> findModeratorsByClasseId(@Param("classeId") String classeId);
}