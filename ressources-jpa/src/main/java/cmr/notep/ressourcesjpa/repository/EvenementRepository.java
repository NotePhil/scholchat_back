package cmr.notep.ressourcesjpa.repository;

import cmr.notep.modele.EtatEvenement;
import cmr.notep.ressourcesjpa.dao.EvenementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EvenementRepository extends JpaRepository<EvenementEntity, String> {

    // Find all events for a specific canal
    List<EvenementEntity> findByCanalId(String canalId);

    // Find all events in a specific state (optional, if needed)
    List<EvenementEntity> findByEtat(EtatEvenement etat);

    // Find all events within a specific time range (optional, if needed)
    List<EvenementEntity> findByHeureDebutBetween(LocalDateTime start, LocalDateTime end);
}