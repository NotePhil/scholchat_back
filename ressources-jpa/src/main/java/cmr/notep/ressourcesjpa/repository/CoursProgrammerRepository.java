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


    /** Cours programmés dans l'une des classes (table de liaison ou ancienne colonne classe_id). */
    @Query(value = "SELECT DISTINCT cp.cours_id FROM ressources.cours_programmer cp "
            + "LEFT JOIN ressources.cours_programmer_classes cpc ON cpc.cours_programmer_id = cp.id "
            + "WHERE cpc.classe_id IN (:classeIds) OR cp.classe_id IN (:classeIds)", nativeQuery = true)
    List<String> findCoursIdsProgrammesDansClasses(@Param("classeIds") java.util.Collection<String> classeIds);

    /** Classes d'une programmation de cours (table de liaison ou ancienne colonne classe_id). */
    @Query(value = "SELECT cpc.classe_id FROM ressources.cours_programmer_classes cpc WHERE cpc.cours_programmer_id = :id "
            + "UNION SELECT cp.classe_id FROM ressources.cours_programmer cp WHERE cp.id = :id AND cp.classe_id IS NOT NULL",
            nativeQuery = true)
    List<String> findClasseIdsDeProgrammation(@Param("id") String coursProgrammerId);
}
