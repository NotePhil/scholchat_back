package cmr.notep.ressourcesjpa.repository;

import cmr.notep.modele.EtatUtilisateur;
import cmr.notep.ressourcesjpa.dao.MessagesEntity;
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
    @Query("SELECT u FROM UtilisateursEntity u JOIN u.classes c WHERE c.id = :classeId")
    List<UtilisateursEntity> findByClasseId(@Param("classeId") String classeId);

    @Query("SELECT u.id FROM UtilisateursEntity u WHERE u.admin = true")
    List<String> findAdminUserIds();

    List<UtilisateursEntity> findByAdminTrue();

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO ressources.parents (parents_id) VALUES (:userId) ON CONFLICT DO NOTHING", nativeQuery = true)
    void insertParentRole(@Param("userId") String userId);

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO ressources.eleves (eleves_id, niveau) VALUES (:userId, :niveau) ON CONFLICT DO NOTHING", nativeQuery = true)
    void insertEleveRole(@Param("userId") String userId, @Param("niveau") String niveau);

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO ressources.professeurs (professeurs_id, has_uploaded) VALUES (:userId, false) ON CONFLICT DO NOTHING", nativeQuery = true)
    void insertProfesseurRole(@Param("userId") String userId);

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO ressources.gestionnaires (gestionnaires_id) VALUES (:userId) ON CONFLICT DO NOTHING", nativeQuery = true)
    void insertGestionnaireRole(@Param("userId") String userId);

}
