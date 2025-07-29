package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.CoursProgrammerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoursProgrammerRepository extends JpaRepository<CoursProgrammerEntity, String> {
    List<CoursProgrammerEntity> findByCoursId(String coursId);
    List<CoursProgrammerEntity> findByClasseId(String classeId);
    List<CoursProgrammerEntity> findByParticipantsId(String participantId);
}