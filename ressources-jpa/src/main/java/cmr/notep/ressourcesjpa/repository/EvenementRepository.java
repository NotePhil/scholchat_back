package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.EvenementEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvenementRepository extends JpaRepository<EvenementEntity, String> {
    boolean existsByTitre(String titre);

    @Query("SELECT e FROM EvenementEntity e WHERE e.createur.id = :professeurId")
    List<EvenementEntity> findByCreateurId(@Param("professeurId") String professeurId);

    List<EvenementEntity> findByCreateur_Id(String professeurId);

    /**
     * Paginated — returns all events ordered by heureDebut DESC.
     * Visibility filtering is applied in the service layer after fetching.
     */
    @Query("SELECT e FROM EvenementEntity e ORDER BY e.heureDebut DESC")
    Page<EvenementEntity> findAllOrderByHeurDebutDesc(Pageable pageable);

    /**
     * Count of PUBLIC events (used to build accurate pagination metadata when
     * filtering for public-only visibility is needed without loading all rows).
     */
    @Query("SELECT COUNT(e) FROM EvenementEntity e WHERE e.visibility = 'PUBLIC' OR e.visibility IS NULL")
    long countPublicEvents();
}