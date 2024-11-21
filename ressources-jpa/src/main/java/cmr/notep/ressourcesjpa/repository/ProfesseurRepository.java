package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.ProfesseurEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfesseurRepository extends JpaRepository<ProfesseurEntity, Long> {
    boolean existsByMatriculeProfesseur(String matriculeProfesseur); // Check for unique matricule
}
