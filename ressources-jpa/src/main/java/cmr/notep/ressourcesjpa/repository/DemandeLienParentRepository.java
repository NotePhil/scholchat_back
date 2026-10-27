package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.DemandeLienParentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface DemandeLienParentRepository extends JpaRepository<DemandeLienParentEntity, String> {
    List<DemandeLienParentEntity> findByEleveIdOrderByDateDemandeDesc(String eleveId);

    List<DemandeLienParentEntity> findByParentIdOrderByDateDemandeDesc(String parentId);

    List<DemandeLienParentEntity> findByEtatOrderByDateDemandeAsc(String etat);

    List<DemandeLienParentEntity> findByClasseIdAndEtatOrderByDateDemandeAsc(String classeId, String etat);

    List<DemandeLienParentEntity> findByParentIdAndEleveIdAndEtat(String parentId, String eleveId, String etat);

    List<DemandeLienParentEntity> findByParentIdAndEleveIdAndEtatOrderByDateReponseAsc(String parentId, String eleveId, String etat);

    List<DemandeLienParentEntity> findByEleveIdAndEtat(String eleveId, String etat);

    boolean existsByParentIdAndEleveIdAndClasseIdAndEtatIn(String parentId, String eleveId, String classeId, Collection<String> etats);

    long countByParentIdAndEleveIdAndEtat(String parentId, String eleveId, String etat);

    /** Classes d'un élève majeur que le parent peut lire (suivi ACCEPTEE). */
    @Query("SELECT d.classeId FROM DemandeLienParentEntity d WHERE d.parentId = :parentId AND d.eleveId = :eleveId "
            + "AND d.etat = 'ACCEPTEE' AND d.classeId IS NOT NULL")
    List<String> findClassesSuivies(@Param("parentId") String parentId, @Param("eleveId") String eleveId);

    /**
     * Décision sur une demande encore EN_ATTENTE (la première décision l'emporte) : renvoie 0 si la demande a déjà
     * été traitée entre-temps.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE DemandeLienParentEntity d SET d.etat = :etat, d.dateReponse = :date, d.motif = :motif, "
            + "d.traitePar = :par WHERE d.id = :id AND d.etat = 'EN_ATTENTE'")
    int deciderSiEnAttente(@Param("id") String id, @Param("etat") String etat, @Param("date") LocalDateTime date,
                           @Param("motif") String motif, @Param("par") String par);

    /** Retrait d'un suivi accordé (ACCEPTEE → RETIREE) ; 0 si la demande n'est plus acceptée. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE DemandeLienParentEntity d SET d.etat = 'RETIREE', d.dateReponse = :date, d.motif = :motif, "
            + "d.traitePar = :par WHERE d.id = :id AND d.etat = 'ACCEPTEE'")
    int retirerSiAcceptee(@Param("id") String id, @Param("date") LocalDateTime date, @Param("motif") String motif,
                          @Param("par") String par);
}
