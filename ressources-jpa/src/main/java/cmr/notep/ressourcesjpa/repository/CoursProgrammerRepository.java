package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.CoursProgrammerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoursProgrammerRepository extends JpaRepository<CoursProgrammerEntity, String> {
    List<CoursProgrammerEntity> findByCoursId(String coursId);
    List<CoursProgrammerEntity> findByProfesseurId(String professeurId);

    @Query("SELECT cp FROM CoursProgrammerEntity cp JOIN cp.classes c WHERE c.id = :classeId")
    List<CoursProgrammerEntity> findByClasseId(@Param("classeId") String classeId);

    @Query("SELECT cp FROM CoursProgrammerEntity cp JOIN cp.participants p WHERE p.id = :participantId")
    List<CoursProgrammerEntity> findByParticipantId(@Param("participantId") String participantId);

    @Query("""
    SELECT cp FROM CoursProgrammerEntity cp
    JOIN cp.classes c
    JOIN AccederEntity a ON a.classeId = c.id
    WHERE a.utilisateurId = :userId
""")
    List<CoursProgrammerEntity> findByUserAccess(@Param("userId") String userId);

    /** Nombre de programmations du cours dans la classe (table de jointure, ou ancienne colonne classe_id). */
    @Query(value = """
    SELECT COUNT(*) FROM ressources.cours_programmer cp
    WHERE cp.cours_id = :coursId
      AND (cp.classe_id = :classeId
           OR EXISTS (SELECT 1 FROM ressources.cours_programmer_classes cpc
                      WHERE cpc.cours_programmer_id = cp.id AND cpc.classe_id = :classeId))
""", nativeQuery = true)
    long countProgrammationsDansClasse(@Param("coursId") String coursId, @Param("classeId") String classeId);

}