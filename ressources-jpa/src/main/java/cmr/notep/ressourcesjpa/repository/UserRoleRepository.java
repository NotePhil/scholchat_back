package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRoleEntity, String> {

    List<UserRoleEntity> findByUtilisateurId(String utilisateurId);

    List<UserRoleEntity> findByUtilisateurIdAndIsActiveTrue(String utilisateurId);

    Optional<UserRoleEntity> findByUtilisateurIdAndRoleType(String utilisateurId, String roleType);

    boolean existsByUtilisateurIdAndRoleType(String utilisateurId, String roleType);
}
