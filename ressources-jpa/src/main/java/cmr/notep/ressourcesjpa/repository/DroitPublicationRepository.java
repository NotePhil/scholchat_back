package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.DroitPublicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DroitPublicationRepository extends JpaRepository<DroitPublicationEntity, Long> {
    List<DroitPublicationEntity> findByClasseId(String classeId);
    List<DroitPublicationEntity> findByUtilisateurId(String utilisateurId);
    boolean existsByUtilisateurIdAndClasseId(String utilisateurId, String classeId);
    Optional<DroitPublicationEntity> findByUtilisateurIdAndClasseId(String utilisateurId, String classeId);
}