package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.Etablissement;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.EtablissementEntity;
import cmr.notep.ressourcesjpa.repository.EtablissementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j

public class EtablissementBusiness {
    private final DaoAccessorService daoAccessorService;

    public EtablissementBusiness(DaoAccessorService daoAccessorService) {
        this.daoAccessorService = daoAccessorService;
    }

    public Etablissement creerEtablissement(Etablissement etablissement) {

            EtablissementEntity entity = dozerMapperBean.map(etablissement, EtablissementEntity.class);
            EtablissementEntity saved = daoAccessorService.getRepository(EtablissementRepository.class).save(entity);

            return dozerMapperBean.map(saved, Etablissement.class);
    }

    public Etablissement modifierEtablissement(String id, Etablissement etablissementModifie) {
            EtablissementRepository repo = daoAccessorService.getRepository(EtablissementRepository.class);
            EtablissementEntity existing = repo.findById(id)
                    .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Établissement non trouvé avec l'ID: " + id));

            // Update fields
            existing.setNom(etablissementModifie.getNom());

            EtablissementEntity updated = repo.save(existing);
            log.info("Etablissement modifié avec succès: {}", updated.getId());
            return dozerMapperBean.map(updated, Etablissement.class);

    }

    public void supprimerEtablissement(String id) {
            EtablissementRepository repo = daoAccessorService.getRepository(EtablissementRepository.class);
            if (!repo.existsById(id)) {
                throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Établissement non trouvé avec l'ID: " + id);
            }
            repo.deleteById(id);
    }

    public Etablissement obtenirEtablissementParId(String id) {
            EtablissementRepository repo = daoAccessorService.getRepository(EtablissementRepository.class);
            EtablissementEntity entity = repo.findById(id)
                    .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Établissement non trouvé avec l'ID: " + id));
            return dozerMapperBean.map(entity, Etablissement.class);
    }

    public List<Etablissement> obtenirTousLesEtablissements() {
            EtablissementRepository repo = daoAccessorService.getRepository(EtablissementRepository.class);
            return repo.findAll()
                    .stream()
                    .map(entity -> dozerMapperBean.map(entity, Etablissement.class))
                    .collect(Collectors.toList());
    }

}
