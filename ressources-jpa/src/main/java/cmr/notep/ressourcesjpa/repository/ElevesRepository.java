package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.ElevesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ElevesRepository extends JpaRepository<ElevesEntity, String> {
    @Query("SELECT e FROM ElevesEntity e JOIN e.parents p WHERE p.id = :parentId")
    List<ElevesEntity> findByParentId(@Param("parentId") String parentId);

    @Query("""
            SELECT DISTINCT e FROM ElevesEntity e
            JOIN AccederEntity ae ON ae.utilisateurId = e.id
            WHERE ae.classeId IN (
                SELECT d.classeId FROM DroitPublicationEntity d WHERE d.utilisateurId = :professeurId
                UNION
                SELECT a.classeId FROM AccederEntity a WHERE a.utilisateurId = :professeurId
                UNION
                SELECT c.id FROM ClassesEntity c WHERE c.moderator.id = :professeurId
            )
            """)
    List<ElevesEntity> findElevesByProfesseurId(@Param("professeurId") String professeurId);
}