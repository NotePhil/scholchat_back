package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.ParentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParentsRepository extends JpaRepository<ParentsEntity, String> {
    @Query("SELECT p FROM ParentsEntity p JOIN p.enfants e WHERE e.id = :eleveId")
    List<ParentsEntity> findByEnfantId(@Param("eleveId") String eleveId);

    @Query("""
            SELECT DISTINCT p FROM ParentsEntity p
            JOIN p.enfants e
            JOIN AccederEntity ae ON ae.utilisateurId = e.id
            WHERE ae.classeId IN (
                SELECT d.classeId FROM DroitPublicationEntity d WHERE d.utilisateurId = :professeurId
                UNION
                SELECT a.classeId FROM AccederEntity a WHERE a.utilisateurId = :professeurId
                UNION
                SELECT c.id FROM ClassesEntity c WHERE c.moderator.id = :professeurId
            )
            """)
    List<ParentsEntity> findParentsByProfesseurId(@Param("professeurId") String professeurId);
}
