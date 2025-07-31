package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.Evenement;
import cmr.notep.interfaces.modeles.Media;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.EvenementEntity;
import cmr.notep.ressourcesjpa.dao.MediaEntity;
import cmr.notep.ressourcesjpa.dao.ProfesseursEntity;
import cmr.notep.ressourcesjpa.repository.EvenementRepository;
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
        try {
            // Check for duplicate title
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

            // Handle media properly with all required fields
            if (evenement.getMedias() != null && !evenement.getMedias().isEmpty()) {
                List<MediaEntity> mediaEntities = evenement.getMedias().stream()
                        .map(media -> {
                            MediaEntity mediaEntity = dozerMapperBean.map(media, MediaEntity.class);
                            mediaEntity.setEvenement(entity); // Set the relationship

                            // Set all required fields to avoid null constraint violations
                            if (mediaEntity.getBucketName() == null) {
                                mediaEntity.setBucketName("default-bucket"); // Set a default bucket name
                            }
                            if (mediaEntity.getContentType() == null) {
                                mediaEntity.setContentType("application/octet-stream"); // Set a default content type
                            }
                            // FIX: Set mediaType field that was missing
                            if (mediaEntity.getMediaType() == null) {
                                // Determine mediaType based on contentType
                                String contentType = mediaEntity.getContentType();
                                if (contentType != null && contentType.startsWith("image/")) {
                                    mediaEntity.setMediaType("IMAGE");
                                } else {
                                    mediaEntity.setMediaType("DOCUMENT");
                                }
                            }
                            // Set fileName if null
                            if (mediaEntity.getFileName() == null) {
                                mediaEntity.setFileName("event-file");
                            }
                            // Set fileSize if null
                            if (mediaEntity.getFileSize() == null) {
                                mediaEntity.setFileSize(0L);
                            }
                            // Set filePath if null
                            if (mediaEntity.getFilePath() == null) {
                                mediaEntity.setFilePath("/default/path");
                            }

                            return mediaEntity;
                        })
                        .collect(Collectors.toList());
                entity.setMedias(mediaEntities);
            }

            // Save the event
            EvenementEntity savedEntity = daoAccessorService.getRepository(EvenementRepository.class).save(entity);

            // Map back to DTO for response
            Evenement savedEvenement = dozerMapperBean.map(savedEntity, Evenement.class);
            savedEvenement.setCreateurId(savedEntity.getCreateur().getId());

            log.info("Event created successfully with ID: {}", savedEntity.getId());
            return savedEvenement;

        } catch (SchoolException e) {
            log.error("Business logic error creating event: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error saving event: {}", e.getMessage(), e);
            throw new SchoolException(SchoolErrorCode.INTERNAL_ERROR,
                    "Erreur lors de la sauvegarde de l'événement: " + e.getMessage());
        }
    }

    public Evenement mettreAJourEvenement(String id, Evenement evenement) {
        try {
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
            Evenement result = dozerMapperBean.map(updated, Evenement.class);
            result.setCreateurId(updated.getCreateur().getId()); // Set creator ID properly

            log.info("Event updated successfully with ID: {}", id);
            return result;
        } catch (SchoolException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating event with ID {}: {}", id, e.getMessage(), e);
            throw new SchoolException(SchoolErrorCode.INTERNAL_ERROR,
                    "Erreur lors de la mise à jour de l'événement: " + e.getMessage());
        }
    }

    public List<Evenement> obtenirTousEvenements() {
        try {
            List<EvenementEntity> entities = daoAccessorService.getRepository(EvenementRepository.class).findAll();
            log.info("Retrieved {} events from database", entities.size());

            return entities.stream()
                    .map(e -> {
                        try {
                            Evenement evenement = dozerMapperBean.map(e, Evenement.class);
                            if (e.getCreateur() != null) {
                                evenement.setCreateurId(e.getCreateur().getId()); // Set creator ID properly
                            }
                            return evenement;
                        } catch (Exception ex) {
                            log.error("Error mapping event with ID {}: {}", e.getId(), ex.getMessage());
                            // Return a basic event object if mapping fails
                            Evenement fallbackEvent = new Evenement();
                            fallbackEvent.setId(e.getId());
                            fallbackEvent.setTitre(e.getTitre());
                            fallbackEvent.setDescription(e.getDescription());
                            fallbackEvent.setLieu(e.getLieu());
                            fallbackEvent.setEtat(e.getEtat());
                            fallbackEvent.setHeureDebut(e.getHeureDebut());
                            fallbackEvent.setHeureFin(e.getHeureFin());
                            if (e.getCreateur() != null) {
                                fallbackEvent.setCreateurId(e.getCreateur().getId());
                            }
                            return fallbackEvent;
                        }
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving all events: {}", e.getMessage(), e);
            throw new SchoolException(SchoolErrorCode.INTERNAL_ERROR,
                    "Erreur lors de la récupération des événements: " + e.getMessage());
        }
    }

    public void supprimerEvenement(String id) {
        try {
            EvenementRepository repo = daoAccessorService.getRepository(EvenementRepository.class);
            if (!repo.existsById(id)) {
                throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Événement non trouvé avec l'ID: " + id);
            }
            repo.deleteById(id);
            log.info("Event deleted successfully with ID: {}", id);
        } catch (SchoolException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting event with ID {}: {}", id, e.getMessage(), e);
            throw new SchoolException(SchoolErrorCode.INTERNAL_ERROR,
                    "Erreur lors de la suppression de l'événement: " + e.getMessage());
        }
    }

    public Evenement obtenirEvenementParId(String id) {
        try {
            EvenementEntity entity = daoAccessorService.getRepository(EvenementRepository.class)
                    .findById(id)
                    .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Événement non trouvé avec l'ID: " + id));

            Evenement evenement = dozerMapperBean.map(entity, Evenement.class);
            if (entity.getCreateur() != null) {
                evenement.setCreateurId(entity.getCreateur().getId()); // Set creator ID properly
            }
            return evenement;
        } catch (SchoolException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving event with ID {}: {}", id, e.getMessage(), e);
            throw new SchoolException(SchoolErrorCode.INTERNAL_ERROR,
                    "Erreur lors de la récupération de l'événement: " + e.getMessage());
        }
    }

    public List<Evenement> obtenirEvenementsParProfesseur(String professeurId) {
        try {
            List<EvenementEntity> entities = daoAccessorService.getRepository(EvenementRepository.class)
                    .findByCreateurId(professeurId); // This now works with the fixed repository method

            log.info("Retrieved {} events for professor ID: {}", entities.size(), professeurId);

            return entities.stream()
                    .map(e -> {
                        try {
                            Evenement evenement = dozerMapperBean.map(e, Evenement.class);
                            if (e.getCreateur() != null) {
                                evenement.setCreateurId(e.getCreateur().getId()); // Set creator ID properly
                            }
                            return evenement;
                        } catch (Exception ex) {
                            log.error("Error mapping event with ID {} for professor {}: {}", e.getId(), professeurId, ex.getMessage());
                            // Return a basic event object if mapping fails
                            Evenement fallbackEvent = new Evenement();
                            fallbackEvent.setId(e.getId());
                            fallbackEvent.setTitre(e.getTitre());
                            fallbackEvent.setDescription(e.getDescription());
                            fallbackEvent.setLieu(e.getLieu());
                            fallbackEvent.setEtat(e.getEtat());
                            fallbackEvent.setHeureDebut(e.getHeureDebut());
                            fallbackEvent.setHeureFin(e.getHeureFin());
                            if (e.getCreateur() != null) {
                                fallbackEvent.setCreateurId(e.getCreateur().getId());
                            }
                            return fallbackEvent;
                        }
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving events for professor {}: {}", professeurId, e.getMessage(), e);
            throw new SchoolException(SchoolErrorCode.INTERNAL_ERROR,
                    "Erreur lors de la récupération des événements du professeur: " + e.getMessage());
        }
    }
}