package cmr.notep.ressourcesjpa.repository;

import cmr.notep.modele.EtatCours;
import cmr.notep.ressourcesjpa.dao.CoursEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoursRepository extends JpaRepository<CoursEntity, String> {
    List<CoursEntity> findByRedacteurId(String professeurId);
    List<CoursEntity> findByMatieresId(String matiereId);
    List<CoursEntity> findByEtat(EtatCours etat);
    List<CoursEntity> findByRestriction(String restriction);
    @Query("SELECT c FROM CoursEntity c WHERE c.restriction = 'PUBLIC' OR c.redacteur.id = :userId")
    List<CoursEntity> findAccessibleCours(@Param("userId") String userId);
}