package cmr.notep.business.business;

import cmr.notep.interfaces.modeles.Classes;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.ClassesEntity;
import cmr.notep.ressourcesjpa.dao.ElevesEntity;
import cmr.notep.ressourcesjpa.dao.EtablissementEntity;
import cmr.notep.ressourcesjpa.dao.ParentsEntity;
import cmr.notep.ressourcesjpa.repository.ClassesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
public class ClassesBusiness {

    private final DaoAccessorService daoAccessorService;

    public ClassesBusiness(DaoAccessorService daoAccessorService) {
        this.daoAccessorService = daoAccessorService;
    }

    public Classes createClass(Classes classes) {
        ClassesEntity savedEntity = daoAccessorService.getRepository(ClassesRepository.class)
                .save(dozerMapperBean.map(classes, ClassesEntity.class));
        return dozerMapperBean.map(savedEntity, Classes.class);
    }

    public Classes updateClass(String id, Classes updatedClass) {
        Optional<ClassesEntity> existingClassOptional = daoAccessorService.getRepository(ClassesRepository.class)
                .findById(id);

        if (!existingClassOptional.isPresent()) {
            throw new RuntimeException("Class not found");
        }

        ClassesEntity existingClass = existingClassOptional.get();

        // Update fields
        existingClass.setNom(updatedClass.getNom());
        existingClass.setNiveau(updatedClass.getNiveau());
        existingClass.setEtat(updatedClass.getEtat());
        existingClass.setDateCreation(updatedClass.getDateCreation());

        existingClass.setEtablissement(dozerMapperBean.map(updatedClass.getEtablissement(), EtablissementEntity.class));

        // map Parents DTO to ParentsEntity
        existingClass.setParents(updatedClass.getParents()
                .stream()
                .map(p -> dozerMapperBean.map(p, ParentsEntity.class))
                .collect(Collectors.toList()));

        // map Eleves DTO to ElevesEntity
        existingClass.setEleves(updatedClass.getEleves()
                .stream()
                .map(e -> dozerMapperBean.map(e, ElevesEntity.class))
                .collect(Collectors.toList()));

        // Save updated class entity
        ClassesEntity savedClass = daoAccessorService.getRepository(ClassesRepository.class).save(existingClass);

        // Return the DTO after saving (correct mapping from ClassesEntity to Classes DTO)
        return dozerMapperBean.map(savedClass, Classes.class);
    }



    public void deleteClass(String id) {
        daoAccessorService.getRepository(ClassesRepository.class).deleteById(id);
    }

    public Classes getClassById(String id) {
        return dozerMapperBean.map(daoAccessorService.getRepository(ClassesRepository.class)
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Class not found")), Classes.class);
    }

    public List<Classes> getAllClasses() {
        return daoAccessorService.getRepository(ClassesRepository.class).findAll()
                .stream().map(c -> dozerMapperBean.map(c, Classes.class))
                .collect(Collectors.toList());
    }
}
