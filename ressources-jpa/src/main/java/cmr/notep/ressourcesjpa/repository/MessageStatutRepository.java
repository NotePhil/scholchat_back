package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.MessageStatutEntity;
import cmr.notep.ressourcesjpa.dao.MessageStatutId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageStatutRepository extends JpaRepository<MessageStatutEntity, MessageStatutId> {

    List<MessageStatutEntity> findByIdUtilisateurIdAndFavoriTrue(String utilisateurId);

    List<MessageStatutEntity> findByIdUtilisateurIdAndLuFalse(String utilisateurId);

    long countByIdUtilisateurIdAndLuFalse(String utilisateurId);
}
