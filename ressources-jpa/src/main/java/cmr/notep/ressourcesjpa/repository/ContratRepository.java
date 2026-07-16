package cmr.notep.ressourcesjpa.repository;

import cmr.notep.modele.StatutContrat;
import cmr.notep.ressourcesjpa.dao.ContratEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContratRepository extends JpaRepository<ContratEntity, String> {
    List<ContratEntity> findByClasseIdOrderByDateCreationDesc(String classeId);
    List<ContratEntity> findByEtablissementIdOrderByDateCreationDesc(String etablissementId);

    Optional<ContratEntity> findFirstByClasseIdAndStatutOrderByDateCreationDesc(String classeId, StatutContrat statut);
    Optional<ContratEntity> findFirstByEtablissementIdAndStatutOrderByDateCreationDesc(String etablissementId, StatutContrat statut);

    long countByEtablissementIdAndStatut(String etablissementId, StatutContrat statut);

    List<ContratEntity> findByStatutAndDateFinBefore(StatutContrat statut, LocalDateTime dateFin);
    List<ContratEntity> findByStatutAndDateFinBetween(StatutContrat statut, LocalDateTime start, LocalDateTime end);
    List<ContratEntity> findByStatut(StatutContrat statut);

    Optional<ContratEntity> findByRenewalToken(String renewalToken);
}
