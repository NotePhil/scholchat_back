package cmr.notep.ressourcesjpa.repository;

import cmr.notep.modele.EtatClasse;
import cmr.notep.ressourcesjpa.dao.HistoActivationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HistoActivationRepository extends JpaRepository<HistoActivationEntity, String> {
    List<HistoActivationEntity> findByClasseId(String classeId);
    List<HistoActivationEntity> findByUtilisateurId(String utilisateurId);
    List<HistoActivationEntity> findByIsActive(boolean isActive);
    List<HistoActivationEntity> findByEtatClasse(EtatClasse etatClasse);
    boolean existsByClasseIdAndIsActive(String classeId, boolean isActive);

    // Add this new method
    Optional<HistoActivationEntity> findTopByClasseIdOrderByDateActivationDesc(String classeId);
}