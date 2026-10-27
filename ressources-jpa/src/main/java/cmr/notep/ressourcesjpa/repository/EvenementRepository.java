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
    @Query("SELECT e FROM EvenementEntity e ORDER BY e.datePublication DESC NULLS LAST, e.heureDebut DESC")
    Page<EvenementEntity> findAllOrderByHeurDebutDesc(Pageable pageable);

    /**
     * Fil d'un élève (ou d'un parent pour l'enfant sélectionné) : événements publics (sans classe
     * ciblée) + événements ciblant au moins une des classes données. Filtré en base pour que la
     * pagination soit exacte. {@code classeIds} ne doit pas être vide (passer une valeur bidon).
     */
    @Query(value = "SELECT e FROM EvenementEntity e WHERE e.visibility = 'PUBLIC' OR e.visibility IS NULL "
            + "OR EXISTS (SELECT 1 FROM EvenementEntity e2 JOIN e2.classes c WHERE e2 = e AND c.id IN :classeIds) "
            + "ORDER BY e.datePublication DESC NULLS LAST, e.heureDebut DESC",
           countQuery = "SELECT COUNT(e) FROM EvenementEntity e WHERE e.visibility = 'PUBLIC' OR e.visibility IS NULL "
            + "OR EXISTS (SELECT 1 FROM EvenementEntity e2 JOIN e2.classes c WHERE e2 = e AND c.id IN :classeIds)")
    Page<EvenementEntity> findFilPourClasses(@Param("classeIds") java.util.Collection<String> classeIds, Pageable pageable);

    /**
     * Count of PUBLIC events (used to build accurate pagination metadata when
     * filtering for public-only visibility is needed without loading all rows).
     */
    @Query("SELECT COUNT(e) FROM EvenementEntity e WHERE e.visibility = 'PUBLIC' OR e.visibility IS NULL")
    long countPublicEvents();

    /**
     * Actualités générales : événements publics (visibilité PUBLIC ou absente) qui ne ciblent aucune classe.
     * Seul fil visible par un parent dont aucun enfant n'a encore été accepté dans une classe.
     */
    @Query(value = "SELECT e FROM EvenementEntity e WHERE (e.visibility = 'PUBLIC' OR e.visibility IS NULL) "
            + "AND e.classes IS EMPTY ORDER BY e.datePublication DESC NULLS LAST, e.heureDebut DESC",
           countQuery = "SELECT COUNT(e) FROM EvenementEntity e WHERE (e.visibility = 'PUBLIC' OR e.visibility IS NULL) "
            + "AND e.classes IS EMPTY")
    Page<EvenementEntity> findPublicsGeneraux(Pageable pageable);

    /** L'événement est-il une actualité générale (publique, sans classe ciblée) ? */
    @Query("SELECT COUNT(e) > 0 FROM EvenementEntity e WHERE e.id = :id "
            + "AND (e.visibility = 'PUBLIC' OR e.visibility IS NULL) AND e.classes IS EMPTY")
    boolean estPublicGeneral(@Param("id") String id);

    /** Le média (id ou chemin) est-il attaché à une actualité générale (publique, sans classe ciblée) ? */
    @Query("SELECT COUNT(m) > 0 FROM MediaEntity m JOIN m.evenement e WHERE (m.id = :ref OR m.filePath = :ref) "
            + "AND (e.visibility = 'PUBLIC' OR e.visibility IS NULL) AND e.classes IS EMPTY")
    boolean estMediaDePublicGeneral(@Param("ref") String mediaIdOuChemin);
}
