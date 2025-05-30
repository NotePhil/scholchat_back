
package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.Evenement;
import cmr.notep.interfaces.modeles.Matiere;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.EvenementEntity;
import cmr.notep.ressourcesjpa.dao.MatiereEntity;
import cmr.notep.ressourcesjpa.dao.ProfesseursEntity;
import cmr.notep.ressourcesjpa.repository.EvenementRepository;
import cmr.notep.ressourcesjpa.repository.MatiereRepository;
import cmr.notep.ressourcesjpa.repository.ProfesseursRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
public class EvenementBusiness {

    private final DaoAccessorService daoAccessorService;

    public EvenementBusiness(DaoAccessorService daoAccessorService) {
        this.daoAccessorService = daoAccessorService;
    }

    public Evenement creerEvenement(Evenement evenement) {
        if (daoAccessorService.getRepository(EvenementRepository.class).existsByTitre(evenement.getTitre())) {
            throw new SchoolException(SchoolErrorCode.DUPLICATE_RESOURCE,
                    "Un événement avec ce titre existe déjà: " + evenement.getTitre());
        }
        // Map the event DTO to entity
        EvenementEntity entity = dozerMapperBean.map(evenement, EvenementEntity.class);

        // Fetch and set the creator
        ProfesseursEntity createur = daoAccessorService.getRepository(ProfesseursRepository.class)
                .findById(evenement.getCreateurId())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND,
                        "Professeur introuvable avec l'ID: " + evenement.getCreateurId()));
        entity.setCreateur(createur);

        // Set participants if needed
        if (evenement.getParticipantsIds() != null) {
            entity.setParticipantsIds(evenement.getParticipantsIds());
        }

        // Save the event
        EvenementEntity savedEntity = daoAccessorService.getRepository(EvenementRepository.class).save(entity);

        // Map back to DTO for response
        Evenement savedEvenement = dozerMapperBean.map(savedEntity, Evenement.class);
        savedEvenement.setCreateurId(savedEntity.getCreateur().getId());

        return savedEvenement;
    }

    public Evenement mettreAJourEvenement(String id, Evenement evenement) {
        EvenementRepository repo = daoAccessorService.getRepository(EvenementRepository.class);
        EvenementEntity existing = repo.findById(id)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Événement non trouvé avec l'ID: " + id));

        // Update fields
        existing.setTitre(evenement.getTitre());
        existing.setDescription(evenement.getDescription());
        existing.setLieu(evenement.getLieu());
        existing.setEtat(evenement.getEtat());
        existing.setHeureDebut(evenement.getHeureDebut());
        existing.setHeureFin(evenement.getHeureFin());

        EvenementEntity updated = repo.save(existing);
        return dozerMapperBean.map(updated, Evenement.class);
    }

    public List<Evenement> obtenirTousEvenements() {
        return daoAccessorService.getRepository(EvenementRepository.class).findAll()
                .stream()
                .map(e -> dozerMapperBean.map(e, Evenement.class))
                .collect(Collectors.toList());
    }

    public void supprimerEvenement(String id) {
        EvenementRepository repo = daoAccessorService.getRepository(EvenementRepository.class);
        if (!repo.existsById(id)) {
            throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Événement non trouvé avec l'ID: " + id);
        }
        repo.deleteById(id);
    }

    public Evenement obtenirEvenementParId(String id) {
        EvenementEntity entity = daoAccessorService.getRepository(EvenementRepository.class)
                .findById(id)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Événement non trouvé avec l'ID: " + id));
        return dozerMapperBean.map(entity, Evenement.class);
    }

    public List<Evenement> obtenirEvenementsParProfesseur(String professeurId) {
        return daoAccessorService.getRepository(EvenementRepository.class)
                .findByCreateurId(professeurId)
                .stream()
                .map(e -> dozerMapperBean.map(e, Evenement.class))
                .collect(Collectors.toList());
    }
}