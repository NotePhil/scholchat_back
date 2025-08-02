package cmr.notep.business.business;

import cmr.notep.interfaces.modeles.CoursProgrammer;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.*;
import cmr.notep.ressourcesjpa.repository.ClassesRepository;
import cmr.notep.ressourcesjpa.repository.CoursProgrammerRepository;
import cmr.notep.ressourcesjpa.repository.CoursRepository;
import cmr.notep.ressourcesjpa.repository.UtilisateursRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
public class CoursProgrammerBusiness {

    private final DaoAccessorService daoAccessorService;

    public CoursProgrammerBusiness(DaoAccessorService daoAccessorService) {
        this.daoAccessorService = daoAccessorService;
    }

    public CoursProgrammer programmerCours(CoursProgrammer coursProgrammer) {
        CoursProgrammerEntity entity = dozerMapperBean.map(coursProgrammer, CoursProgrammerEntity.class);

        // Mapping des relations
        entity.setCours(daoAccessorService.getRepository(CoursRepository.class)
                .findById(coursProgrammer.getId())
                .orElseThrow(() -> new RuntimeException("Cours introuvable")));

        if (coursProgrammer.getClasseId() != null) {
            entity.setClasse(daoAccessorService.getRepository(ClassesRepository.class)
                    .findById(coursProgrammer.getClasseId())
                    .orElseThrow(() -> new RuntimeException("Classe introuvable")));
        }

        // Mapping des participants
        if (coursProgrammer.getParticipantsIds() != null) {
            List<UtilisateursEntity> participants = coursProgrammer.getParticipantsIds().stream()
                    .map(id -> daoAccessorService.getRepository(UtilisateursRepository.class)
                            .findById(id)
                            .orElseThrow(() -> new RuntimeException("Utilisateur introuvable: " + id)))
                    .collect(Collectors.toList());
            entity.setParticipants(participants);
        }

        CoursProgrammerEntity savedEntity = daoAccessorService.getRepository(CoursProgrammerRepository.class).save(entity);
        return dozerMapperBean.map(savedEntity, CoursProgrammer.class);
    }
    public List<CoursProgrammer> obtenirProgrammationParCours(String coursId) {
        return daoAccessorService.getRepository(CoursProgrammerRepository.class)
                .findByCoursId(coursId)
                .stream()
                .map(e -> dozerMapperBean.map(e, CoursProgrammer.class))
                .collect(Collectors.toList());
    }

    public List<CoursProgrammer> obtenirProgrammationParClasse(String classeId) {
        return daoAccessorService.getRepository(CoursProgrammerRepository.class)
                .findByClasseId(classeId)
                .stream()
                .map(e -> dozerMapperBean.map(e, CoursProgrammer.class))
                .collect(Collectors.toList());
    }
}