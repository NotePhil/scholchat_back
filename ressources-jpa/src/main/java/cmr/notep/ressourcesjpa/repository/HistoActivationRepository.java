package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.HistoActivationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoActivationRepository extends JpaRepository<HistoActivationEntity, String> {
    List<HistoActivationEntity> findByClasseId(String classeId);
    List<HistoActivationEntity> findByProfesseurId(String professeurId);
    List<HistoActivationEntity> findByIsActive(boolean isActive);
}