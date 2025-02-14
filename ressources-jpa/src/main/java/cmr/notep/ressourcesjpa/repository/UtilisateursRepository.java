package cmr.notep.ressourcesjpa.repository;

import cmr.notep.modele.EtatUtilisateur;
import cmr.notep.ressourcesjpa.dao.UtilisateursEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Date;

public interface UtilisateursRepository extends JpaRepository<UtilisateursEntity, String> {
    Optional<UtilisateursEntity> findByEmail(String email);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM utilisateurs u WHERE u.etat = :etat AND u.creation_date < :creationDate", nativeQuery = true)
    int deleteByEtatAndCreationDateBefore(@Param("etat") String etat, @Param("creationDate") Date creationDate);

    @Query("SELECT u FROM UtilisateursEntity u WHERE TYPE(u) = ProfesseursEntity AND u.etat = :etat")
    List<UtilisateursEntity> findByEtat(@Param("etat") EtatUtilisateur etat);

}
