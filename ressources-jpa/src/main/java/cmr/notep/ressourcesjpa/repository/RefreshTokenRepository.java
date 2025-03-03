package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findByToken(String token);

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshTokenEntity r WHERE r.utilisateur.id = :userId")
    void deleteByUtilisateurId(@Param("userId") String userId);
}