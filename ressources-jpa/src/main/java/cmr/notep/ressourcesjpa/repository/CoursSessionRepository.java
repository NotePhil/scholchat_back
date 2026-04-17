package cmr.notep.ressourcesjpa.repository;

import cmr.notep.modele.SessionStatus;
import cmr.notep.ressourcesjpa.dao.CoursSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoursSessionRepository extends JpaRepository<CoursSessionEntity, String> {
    Optional<CoursSessionEntity> findByCoursIdAndStatus(String coursId, SessionStatus status);
}
