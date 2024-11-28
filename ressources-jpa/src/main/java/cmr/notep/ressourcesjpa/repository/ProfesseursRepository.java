package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.ProfesseursEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfesseursRepository extends JpaRepository<ProfesseursEntity, Long> {
    boolean existsByMatriculeProfesseur(String matriculeProfesseur); // Check for unique matricule
}
