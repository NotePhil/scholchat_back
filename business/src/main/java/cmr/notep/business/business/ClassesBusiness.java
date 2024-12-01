package cmr.notep.business.business;

import cmr.notep.interfaces.modeles.Classes;
import cmr.notep.interfaces.modeles.Eleves;
import cmr.notep.interfaces.modeles.Parents;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.ClassesEntity;
import cmr.notep.ressourcesjpa.dao.ElevesEntity;
import cmr.notep.ressourcesjpa.dao.EtablissementEntity;
import cmr.notep.ressourcesjpa.dao.ParentsEntity;
import cmr.notep.ressourcesjpa.repository.ClassesRepository;
import cmr.notep.ressourcesjpa.repository.ElevesRepository;
import cmr.notep.ressourcesjpa.repository.EtablissementRepository;
import cmr.notep.ressourcesjpa.repository.ParentsRepository;
import lombok.RequiredArgsConstructor;
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

    public Classes creerClasse(Classes classes) {
        try {
            ClassesEntity savedEntity = daoAccessorService.getRepository(ClassesRepository.class)
                    .save(dozerMapperBean.map(classes, ClassesEntity.class));
            log.info("Classe créée avec succès: {}", savedEntity.getId());
            return dozerMapperBean.map(savedEntity, Classes.class);
        } catch (Exception e) {
            log.error("Erreur lors de la création de la classe", e);
            throw new RuntimeException("Impossible de créer la classe", e);
        }
    }

    public Classes modifierClasse(String idClasse, Classes classeModifiee) {
        try {
            ClassesRepository classesRepository = daoAccessorService.getRepository(ClassesRepository.class);
            // First, check if the class exists
            Optional<ClassesEntity> optionalClasse = classesRepository.findById(idClasse);
            if (optionalClasse.isEmpty()) {
                log.error("Classe non trouvée avec l'ID: {}", idClasse);
                throw new RuntimeException("Classe non trouvée avec l'ID: " + idClasse);
            }
            ClassesEntity classeExistante = optionalClasse.get();
            // Mise à jour des champs
            classeExistante.setNom(classeModifiee.getNom());
            classeExistante.setNiveau(classeModifiee.getNiveau());
            classeExistante.setEtat(classeModifiee.getEtat());
            classeExistante.setDateCreation(classeModifiee.getDateCreation());
            // Reste du code de mise à jour...

            // (Keep your existing implementation)

            ClassesEntity classeSauvegardee = classesRepository.save(classeExistante);
            log.info("Classe modifiée avec succès: {}", idClasse);
            return dozerMapperBean.map(classeSauvegardee, Classes.class);
        } catch (Exception e) {
            log.error("Erreur lors de la modification de la classe", e);
            throw new RuntimeException("Impossible de modifier la classe", e);
        }
    }


    public void supprimerClasse(String idClasse) {
        try {
            ClassesRepository classesRepository = daoAccessorService.getRepository(ClassesRepository.class);
            // Vérifier si la classe existe avant de supprimer
            if (!classesRepository.existsById(idClasse)) {
                log.error("Classe non trouvée avec l'ID: {}", idClasse);
                throw new RuntimeException("Classe non trouvée avec l'ID: " + idClasse);
            }
            classesRepository.deleteById(idClasse);
            log.info("Classe supprimée avec succès: {}", idClasse);
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de la classe", e);
            throw new RuntimeException("Impossible de supprimer la classe", e);
        }
    }
    public Classes obtenirClasseParId(String idClasse) {
        try {
            ClassesRepository classesRepository = daoAccessorService.getRepository(ClassesRepository.class);
            ClassesEntity classeEntity = classesRepository.findById(idClasse)
                    .orElseThrow(() -> new RuntimeException("Classe non trouvée avec l'ID: " + idClasse));
            return dozerMapperBean.map(classeEntity, Classes.class);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la classe", e);
            throw new RuntimeException("Impossible de récupérer la classe", e);
        }
    }

    public List<Classes> obtenirToutesLesClasses() {
        try {
            ClassesRepository classesRepository = daoAccessorService.getRepository(ClassesRepository.class);
            return classesRepository.findAll()
                    .stream()
                    .map(c -> dozerMapperBean.map(c, Classes.class))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de toutes les classes", e);
            throw new RuntimeException("Impossible de récupérer les classes", e);
        }
    }
}
