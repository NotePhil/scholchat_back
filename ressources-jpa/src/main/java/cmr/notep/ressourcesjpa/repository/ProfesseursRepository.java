package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.ProfesseursEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfesseursRepository extends JpaRepository<ProfesseursEntity, String> {
    Optional<ProfesseursEntity> findByMatriculeProfesseur(String matriculeProfesseur);
    
    @Query(value = "SELECT professeur_id FROM ressources.professeur_classes_moderees WHERE classe_id = :classeId", nativeQuery = true)
    List<String> findModeratorIdsForClass(@Param("classeId") String classeId);
    
    @Query(value = "INSERT INTO ressources.professeur_classes_moderees (professeur_id, classe_id) VALUES (:professeurId, :classeId)", nativeQuery = true)
    @org.springframework.data.jpa.repository.Modifying
    void addModeratorToClass(@Param("professeurId") String professeurId, @Param("classeId") String classeId);
    
    @Query(value = "DELETE FROM ressources.professeur_classes_moderees WHERE professeur_id = :professeurId AND classe_id = :classeId", nativeQuery = true)
    @org.springframework.data.jpa.repository.Modifying
    void removeModeratorFromClass(@Param("professeurId") String professeurId, @Param("classeId") String classeId);
}