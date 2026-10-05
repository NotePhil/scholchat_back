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
    /**
     * Cours publics, cours rédigés par l'utilisateur, et cours (même PRIVE) programmés pour une classe
     * à laquelle il a accès ou dont il est participant — sinon l'élève voit la séance sans titre.
     */
    @Query("SELECT c FROM CoursEntity c WHERE c.restriction = 'PUBLIC' OR c.redacteur.id = :userId "
            + "OR EXISTS (SELECT cp.id FROM CoursProgrammerEntity cp JOIN cp.classes cl, AccederEntity a "
            + "           WHERE cp.cours = c AND a.classeId = cl.id AND a.utilisateurId = :userId) "
            + "OR EXISTS (SELECT cp2.id FROM CoursProgrammerEntity cp2 JOIN cp2.participants pa "
            + "           WHERE cp2.cours = c AND pa.id = :userId)")
    List<CoursEntity> findAccessibleCours(@Param("userId") String userId);
}