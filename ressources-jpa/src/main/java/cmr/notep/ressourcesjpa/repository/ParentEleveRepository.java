package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.ParentEleveEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParentEleveRepository extends JpaRepository<ParentEleveEntity, Long> {
    List<ParentEleveEntity> findByParentId(String parentId);
    boolean existsByParentIdAndEleveId(String parentId, String eleveId);

    // Requêtes natives : n'instancient pas ParentsEntity (un compte parent peut aussi être
    // professeur/élève, et Hibernate ne garde qu'un sous-type par id — voir UtilisateursRepository).

    @Query(value = "SELECT eleve_id FROM ressources.parent_eleve WHERE parent_id = :parentId", nativeQuery = true)
    List<String> findEleveIdsByParentId(@Param("parentId") String parentId);

    @Query(value = "SELECT parent_id FROM ressources.parent_eleve WHERE eleve_id = :eleveId", nativeQuery = true)
    List<String> findParentIdsByEleveId(@Param("eleveId") String eleveId);

    @Query(value = "SELECT COUNT(*) > 0 FROM ressources.parent_eleve WHERE eleve_id = :eleveId AND parent_id <> :parentId", nativeQuery = true)
    boolean existsOtherParentForEleve(@Param("eleveId") String eleveId, @Param("parentId") String parentId);

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO ressources.parent_eleve (parent_id, eleve_id) VALUES (:parentId, :eleveId) ON CONFLICT DO NOTHING", nativeQuery = true)
    int insertLien(@Param("parentId") String parentId, @Param("eleveId") String eleveId);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM ressources.parent_eleve WHERE parent_id = :parentId AND eleve_id = :eleveId", nativeQuery = true)
    int deleteLien(@Param("parentId") String parentId, @Param("eleveId") String eleveId);
}