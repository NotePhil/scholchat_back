package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.ProfesseursEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfesseursRepository extends JpaRepository<ProfesseursEntity, String> {
    ProfesseursEntity findByMatriculeProfesseur(String matriculeProfesseur);
}