package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.DroitPublicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface DroitPublicationRepository extends JpaRepository<DroitPublicationEntity, Long> {

    // Get all classes where user has publication rights (any status)
    @Query("SELECT d FROM DroitPublicationEntity d WHERE d.utilisateurId = :userId")
    List<DroitPublicationEntity> findAllClassesByUserId(@Param("userId") String userId);

    @Query("SELECT d FROM DroitPublicationEntity d WHERE d.classeId = :classeId")
    List<DroitPublicationEntity> findAllUsersByClassId(@Param("classeId") String classeId);

    boolean existsByUtilisateurIdAndClasseId(String utilisateurId, String classeId);

    Optional<DroitPublicationEntity> findByUtilisateurIdAndClasseId(String utilisateurId, String classeId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM DroitPublicationEntity d WHERE d.classeId = :classeId")
    void deleteByClasseId(@Param("classeId") String classeId);
}