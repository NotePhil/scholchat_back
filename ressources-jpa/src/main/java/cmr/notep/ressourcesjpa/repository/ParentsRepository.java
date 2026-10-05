package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.ParentsEntity;
import cmr.notep.ressourcesjpa.dao.UtilisateursEntity;
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
            SELECT DISTINCT u FROM UtilisateursEntity u
            JOIN ParentEleveEntity pe ON pe.parentId = u.id
            JOIN AccederEntity ae ON ae.utilisateurId = pe.eleveId
            WHERE ae.classeId IN (
                SELECT d.classeId FROM DroitPublicationEntity d WHERE d.utilisateurId = :professeurId
                UNION
                SELECT a.classeId FROM AccederEntity a WHERE a.utilisateurId = :professeurId
                UNION
                SELECT c.id FROM ClassesEntity c WHERE c.moderator.id = :professeurId
            )
            """)
    // Entité de base (pas ParentsEntity) : un parent peut aussi être professeur/élève, et Hibernate
    // ne matérialise qu'un sous-type par id dans le contexte de persistance.
    List<UtilisateursEntity> findParentsByProfesseurId(@Param("professeurId") String professeurId);
}
