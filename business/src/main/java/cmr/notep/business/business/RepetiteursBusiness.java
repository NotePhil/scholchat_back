package cmr.notep.business.business;

import cmr.notep.interfaces.modeles.Repetiteurs;
import cmr.notep.ressourcesjpa.dao.RepetiteursEntity;
import cmr.notep.ressourcesjpa.repository.RepetiteursRepository;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
public class RepetiteursBusiness {

    private final DaoAccessorService daoAccessorService;

    public RepetiteursBusiness(DaoAccessorService daoAccessorService) {
        this.daoAccessorService = daoAccessorService;
    }

    /**
     * Fetch a répétiteur by ID.
     *
     * @param idRepetiteur ID of the répétiteur to fetch.
     * @return The répétiteur as a model object.
     * @throws RuntimeException if the répétiteur is not found.
     */
    public Repetiteurs obtenirRepetiteurParId(Long idRepetiteur) {
        log.info("Fetching répétiteur by ID: {}", idRepetiteur);
        return dozerMapperBean.map(
                daoAccessorService.getRepository(RepetiteursRepository.class)
                        .findById(idRepetiteur)
                        .orElseThrow(() -> new RuntimeException("Répétiteur not found")),
                Repetiteurs.class
        );
    }

    public List<Repetiteurs> obtenirTousLesRepetiteurs() {
        log.info("Fetching all répétiteurs...");
        return daoAccessorService.getRepository(RepetiteursRepository.class)
                .findAll()
                .stream()
                .map(entity -> dozerMapperBean.map(entity, Repetiteurs.class))
                .collect(Collectors.toList());
    }

    /**
     * Create a new répétiteur.
     *
     * @param repetiteur The répétiteur model to create.
     * @return The created répétiteur as a model object.
     * @throws RuntimeException if mandatory fields are missing.
     */
    public Repetiteurs creerRepetiteur(Repetiteurs repetiteur) {
        log.info("Creating répétiteur: {}", repetiteur);

        // Validate mandatory fields
        if (repetiteur.getPieceIdentite() == null || repetiteur.getPhoto() == null) {
            throw new RuntimeException("Pièce d'identité and Photo cannot be null");
        }

        RepetiteursEntity savedEntity = daoAccessorService.getRepository(RepetiteursRepository.class)
                .save(dozerMapperBean.map(repetiteur, RepetiteursEntity.class));
        return dozerMapperBean.map(savedEntity, Repetiteurs.class);
    }
}
