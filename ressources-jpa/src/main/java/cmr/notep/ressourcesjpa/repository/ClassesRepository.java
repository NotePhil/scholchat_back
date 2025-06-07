package cmr.notep.ressourcesjpa.repository;

import cmr.notep.modele.EtatClasse;
import cmr.notep.ressourcesjpa.dao.ClassesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ClassesRepository extends JpaRepository<ClassesEntity, String> {
    List<ClassesEntity> findByEtat(EtatClasse etat);
}