package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.Canal;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.CanalEntity;
import cmr.notep.ressourcesjpa.dao.ClassesEntity;
import cmr.notep.ressourcesjpa.dao.ProfesseursEntity;
import cmr.notep.ressourcesjpa.repository.CanalRepository;
import cmr.notep.ressourcesjpa.repository.ClassesRepository;
import cmr.notep.ressourcesjpa.repository.ProfesseursRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
public class CanauxBusiness {

    private final DaoAccessorService daoAccessorService;

    public CanauxBusiness(DaoAccessorService daoAccessorService) {
        this.daoAccessorService = daoAccessorService;
    }

    public Canal creerCanal(Canal canaux) throws SchoolException {
        CanalEntity canauxEntity = dozerMapperBean.map(canaux, CanalEntity.class);
        CanalEntity savedEntity = daoAccessorService.getRepository(CanalRepository.class)
                .save(canauxEntity);
        log.info("Canal créé avec succès: {}", savedEntity.getId());
        return dozerMapperBean.map(savedEntity, Canal.class);
    }

    public Canal modifierCanal(String idCanal, Canal canalModifie) throws SchoolException {
        CanalRepository canalRepository = daoAccessorService.getRepository(CanalRepository.class);
        CanalEntity canalExistante = canalRepository.findById(idCanal)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Canal non trouvé avec l'ID: " + idCanal));

        canalExistante.setNom(canalModifie.getNom());
        canalExistante.setDescription(canalModifie.getDescription());

        CanalEntity canalSauvegarde = canalRepository.save(canalExistante);
        log.info("Canal modifié avec succès: {}", idCanal);
        return dozerMapperBean.map(canalSauvegarde, Canal.class);
    }

    public void supprimerCanal(String idCanal) throws SchoolException {
        CanalRepository canauxRepository = daoAccessorService.getRepository(CanalRepository.class);
        if (!canauxRepository.existsById(idCanal)) {
            throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Canal non trouvé avec l'ID: " + idCanal);
        }
        canauxRepository.deleteById(idCanal);
        log.info("Canal supprimé avec succès: {}", idCanal);
    }

    public Canal obtenirCanalParId(String idCanal) throws SchoolException {
        CanalRepository canauxRepository = daoAccessorService.getRepository(CanalRepository.class);
        CanalEntity canalEntity = canauxRepository.findById(idCanal)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Canal non trouvé avec l'ID: " + idCanal));
        return dozerMapperBean.map(canalEntity, Canal.class);
    }

    public List<Canal> obtenirTousLesCanaux() throws SchoolException {
        CanalRepository canauxRepository = daoAccessorService.getRepository(CanalRepository.class);
        return canauxRepository.findAll()
                .stream()
                .map(c -> dozerMapperBean.map(c, Canal.class))
                .collect(Collectors.toList());
    }

    public List<Canal> obtenirCanauxParClasse(String idClasse) throws SchoolException {
        ClassesRepository classesRepository = daoAccessorService.getRepository(ClassesRepository.class);
        ClassesEntity classeEntity = classesRepository.findById(idClasse)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe non trouvée avec l'ID: " + idClasse));
        return classeEntity.getCanaux()
                .stream()
                .map(c -> dozerMapperBean.map(c, Canal.class))
                .collect(Collectors.toList());
    }

    public List<Canal> obtenirCanauxParProfesseur(String idProfesseur) throws SchoolException {
        ProfesseursRepository professeursRepository = daoAccessorService.getRepository(ProfesseursRepository.class);
        ProfesseursEntity professeurEntity = professeursRepository.findById(idProfesseur)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Professeur non trouvé avec l'ID: " + idProfesseur));
        return professeurEntity.getCanaux()
                .stream()
                .map(c -> dozerMapperBean.map(c, Canal.class))
                .collect(Collectors.toList());
    }
}
