package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.ClassesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassesRepository extends JpaRepository<ClassesEntity, String> {
}
