package cmr.notep.business.business;

import cmr.notep.business.security.UserSubtypeService;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.services.NotificationService;
import cmr.notep.interfaces.dto.CoursProgressionDTO;
import cmr.notep.interfaces.modeles.Chapitre;
import cmr.notep.interfaces.modeles.Cours;
import cmr.notep.interfaces.modeles.Matiere;
import cmr.notep.modele.EtatCours;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.*;
import cmr.notep.ressourcesjpa.repository.*;
import cmr.notep.ressourcesjpa.dao.CoursProgrammerEntity;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Transactional
@Component
@Slf4j
public class CoursBusiness {
    private final UserSubtypeService userSubtypeService;
    private final DaoAccessorService daoAccessorService;
    private final NotificationService notificationService;

    public CoursBusiness(DaoAccessorService daoAccessorService, NotificationService notificationService,
            UserSubtypeService userSubtypeService) {
        this.userSubtypeService = userSubtypeService;
        this.daoAccessorService = daoAccessorService;
        this.notificationService = notificationService;
    }


    public Cours creerCours(Cours cours) {
        // Validate professor
        UtilisateursEntity professeur = userSubtypeService.findProfesseur(cours.getRedacteurId())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Professeur introuvable"));

        // Map and save course
        CoursEntity entity = dozerMapperBean.map(cours, CoursEntity.class);
        entity.setId(UUID.randomUUID().toString());
        entity.setRedacteur(professeur);
        entity.setDateCreation(new Date());

        // Set default state if not provided
        if (entity.getEtat() == null) {
            entity.setEtat(EtatCours.BROUILLON);
        }
        if (entity.getEtat() == EtatCours.PUBLIE) {
            entity.setDatePublication(new Date());
        }

        // Set default restriction if not provided
        if (entity.getRestriction() == null) {
            entity.setRestriction("PRIVE");
        }

        // Process subjects (many-to-many)
        if (cours.getMatieres() != null && !cours.getMatieres().isEmpty()) {
            List<MatiereEntity> matieres = new ArrayList<>();
            for (Matiere matiereDto : cours.getMatieres()) {
                MatiereEntity matiereEntity = daoAccessorService.getRepository(MatiereRepository.class)
                        .findById(matiereDto.getId())
                        .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Matière introuvable"));
                matieres.add(matiereEntity);
            }
            entity.setMatieres(matieres);
        }

        // Process chapters if provided - CRITICAL: Set the cours reference for each chapter
        if (cours.getChapitres() != null && !cours.getChapitres().isEmpty()) {
            List<ChapitreEntity> chapitreEntities = new ArrayList<>();
            StringBuilder contentBuilder = new StringBuilder();

            for (Chapitre chapitre : cours.getChapitres()) {
                ChapitreEntity chapitreEntity = dozerMapperBean.map(chapitre, ChapitreEntity.class);

                // Generate UUID for new chapters
                chapitreEntity.setId(UUID.randomUUID().toString());

                // Set the course reference for the chapter
                chapitreEntity.setCours(entity);

                chapitreEntities.add(chapitreEntity);

                // Add to global content
                contentBuilder.append("=== ").append(chapitre.getTitre()).append(" ===\n");
                contentBuilder.append(chapitre.getContenu()).append("\n\n");
            }

            // Set chapters and content - cascade will handle saving
            entity.setChapitres(chapitreEntities);
            entity.setContenu(contentBuilder.toString());
        }

        // Save the course (cascade will save chapters)
        CoursEntity savedEntity = daoAccessorService.getRepository(CoursRepository.class).save(entity);
        log.info("Saved course entity ID: {}", savedEntity.getId());

        // Send notification for course creation
        try {
            String profName = professeur.getPrenom() + " " + professeur.getNom();
            notificationService.createCourseCreatedNotification(
                savedEntity.getId(), savedEntity.getTitre(),
                professeur.getId(), profName, null);
        } catch (Exception e) {
            log.error("Failed to send course creation notification: {}", e.getMessage());
        }

        // Map back to DTO
        Cours result = dozerMapperBean.map(savedEntity, Cours.class);
        result.setRedacteurId(savedEntity.getRedacteur().getId());
        result.setChapitres(mapChapitresToDto(savedEntity.getChapitres(), savedEntity.getId()));

        return result;
    }
    private List<Chapitre> mapChapitresToDto(List<ChapitreEntity> chapitreEntities, String coursId) {
        return chapitreEntities.stream()
                .map(c -> {
                    Chapitre chapitre = dozerMapperBean.map(c, Chapitre.class);
                    chapitre.setCoursId(coursId);
                    // imageUrl is automatically mapped by Dozer
                    return chapitre;
                })
                .collect(Collectors.toList());
    }

    public Cours mettreAJourCours(String coursId, Cours cours) {
        // Find existing course
        CoursEntity existingEntity = daoAccessorService.getRepository(CoursRepository.class)
                .findById(coursId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Cours introuvable"));

        // Validate professor if redacteurId is being updated
        if (cours.getRedacteurId() != null && !cours.getRedacteurId().equals(existingEntity.getRedacteur().getId())) {
            UtilisateursEntity professeur = userSubtypeService.findProfesseur(cours.getRedacteurId())
                    .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Professeur introuvable"));
            existingEntity.setRedacteur(professeur);
        }

        // Update basic fields
        if (cours.getTitre() != null) {
            existingEntity.setTitre(cours.getTitre());
        }
        if (cours.getDescription() != null) {
            existingEntity.setDescription(cours.getDescription());
        }
        if (cours.getEtat() != null) {
            boolean justPublished = cours.getEtat() == EtatCours.PUBLIE
                    && existingEntity.getEtat() != EtatCours.PUBLIE
                    && existingEntity.getDatePublication() == null;
            existingEntity.setEtat(cours.getEtat());
            if (justPublished) {
                existingEntity.setDatePublication(new Date());
            }
        }
        if (cours.getReferences() != null) {
            existingEntity.setReference(cours.getReferences());
        }
        if (cours.getRestriction() != null) {
            existingEntity.setRestriction(cours.getRestriction());
        }

        // Process subjects (many-to-many)
        if (cours.getMatieres() != null) {
            List<MatiereEntity> matieres = new ArrayList<>();
            for (Matiere matiereDto : cours.getMatieres()) {
                MatiereEntity matiereEntity = daoAccessorService.getRepository(MatiereRepository.class)
                        .findById(matiereDto.getId())
                        .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Matière introuvable"));
                matieres.add(matiereEntity);
            }
            existingEntity.setMatieres(matieres);
        }


        if (cours.getChapitres() != null) {
            existingEntity.setContenu(fusionnerChapitres(existingEntity, cours.getChapitres()));
        }

        // Save the updated course
        CoursEntity updatedEntity = daoAccessorService.getRepository(CoursRepository.class).save(existingEntity);
        log.info("Updated course entity ID: {}", updatedEntity.getId());


        Cours result = dozerMapperBean.map(updatedEntity, Cours.class);
        result.setRedacteurId(updatedEntity.getRedacteur().getId());
        result.setChapitres(mapChapitresToDto(updatedEntity.getChapitres(), updatedEntity.getId()));

        return result;
    }


    /**
     * Updates the course's chapters in place and returns the aggregated content.
     * Recreating them would give new ids, and the ON DELETE CASCADE on
     * progression_chapitre would erase students' progress. Chapters are matched by
     * id; when no id is sent at all (older clients), chapters are matched by
     * position instead. Extras are new chapters, missing ones are removed.
     */
    static String fusionnerChapitres(CoursEntity existingEntity, List<Chapitre> chapitres) {
        List<ChapitreEntity> existants = new ArrayList<>(existingEntity.getChapitres());
        existants.sort(java.util.Comparator.comparing(
                (ChapitreEntity c) -> c.getOrdre() == null ? Integer.MAX_VALUE : c.getOrdre()));
        java.util.Map<String, ChapitreEntity> parId = new java.util.HashMap<>();
        existants.forEach(c -> parId.put(c.getId(), c));
        java.util.Set<String> conserves = new java.util.HashSet<>();
        java.util.Set<String> idsDemandes = new java.util.HashSet<>();
        for (Chapitre chapitre : chapitres) {
            if (chapitre.getId() != null) idsDemandes.add(chapitre.getId());
        }

        StringBuilder contentBuilder = new StringBuilder();

        for (Chapitre chapitre : chapitres) {
            ChapitreEntity chapitreEntity = chapitre.getId() != null ? parId.get(chapitre.getId()) : null;
            if (chapitreEntity == null && idsDemandes.isEmpty()) {
                chapitreEntity = existants.stream()
                        .filter(c -> !conserves.contains(c.getId()))
                        .findFirst().orElse(null);
            }
            if (chapitreEntity == null) {
                chapitreEntity = new ChapitreEntity();
                chapitreEntity.setId(UUID.randomUUID().toString());
                chapitreEntity.setCours(existingEntity);
                existingEntity.getChapitres().add(chapitreEntity);
            }
            chapitreEntity.setTitre(chapitre.getTitre());
            chapitreEntity.setDescription(chapitre.getDescription());
            chapitreEntity.setOrdre(chapitre.getOrdre());
            chapitreEntity.setContenu(chapitre.getContenu());
            if (chapitre.getImageUrl() != null) {
                chapitreEntity.setImageUrl(chapitre.getImageUrl());
            }
            conserves.add(chapitreEntity.getId());

            contentBuilder.append("=== ").append(chapitre.getTitre()).append(" ===\n");
            contentBuilder.append(chapitre.getContenu()).append("\n\n");
        }

        existingEntity.getChapitres().removeIf(c -> !conserves.contains(c.getId()));
        return contentBuilder.toString();
    }

    public void supprimerCours(String coursId) {
        // Check if course exists
        CoursEntity coursEntity = daoAccessorService.getRepository(CoursRepository.class)
                .findById(coursId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Cours introuvable"));

        // Check if course is programmed
        List<CoursProgrammerEntity> programmations = daoAccessorService.getRepository(CoursProgrammerRepository.class)
                .findByCoursId(coursId);
        
        if (!programmations.isEmpty()) {
            throw new SchoolException(SchoolErrorCode.OPERATION_INTERDITE, 
                "Impossible de supprimer ce cours car il est déjà programmé. Veuillez d'abord annuler toutes les programmations associées.");
        }

        daoAccessorService.getRepository(CoursRepository.class).delete(coursEntity);
        log.info("Deleted course ID: {}", coursId);
    }

    public Cours obtenirCoursParId(String coursId) {
        CoursEntity coursEntity = daoAccessorService.getRepository(CoursRepository.class)
                .findById(coursId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Cours introuvable"));


        List<ChapitreEntity> chapitres = daoAccessorService.getRepository(ChapitreRepository.class)
                .findByCoursIdOrderByOrdre(coursId);


        if (coursEntity.getChapitres() == null) {
            coursEntity.setChapitres(new ArrayList<>());
        } else {
            coursEntity.getChapitres().clear();
        }
        coursEntity.getChapitres().addAll(chapitres);

        Cours cours = dozerMapperBean.map(coursEntity, Cours.class);
        cours.setRedacteurId(coursEntity.getRedacteur().getId());
        cours.setChapitres(mapChapitresToDto(chapitres, coursId));
        return cours;
    }

    public List<Cours> obtenirCoursParRestriction(String restriction) {
        List<CoursEntity> coursEntities = daoAccessorService.getRepository(CoursRepository.class)
                .findByRestriction(restriction);

        return coursEntities.stream()
                .map(c -> {
                    List<ChapitreEntity> chapitres = daoAccessorService.getRepository(ChapitreRepository.class)
                            .findByCoursIdOrderByOrdre(c.getId());


                    if (c.getChapitres() == null) {
                        c.setChapitres(new ArrayList<>());
                    } else {
                        c.getChapitres().clear();
                    }
                    c.getChapitres().addAll(chapitres);

                    Cours cours = dozerMapperBean.map(c, Cours.class);
                    cours.setRedacteurId(c.getRedacteur().getId());
                    cours.setChapitres(mapChapitresToDto(chapitres, c.getId()));
                    return cours;
                })
                .collect(Collectors.toList());
    }

    public List<Cours> obtenirCoursParProfesseur(String professeurId) {
        List<CoursEntity> coursEntities = daoAccessorService.getRepository(CoursRepository.class)
                .findByRedacteurId(professeurId);

        return coursEntities.stream()
                .map(c -> {

                    List<ChapitreEntity> chapitres = daoAccessorService.getRepository(ChapitreRepository.class)
                            .findByCoursIdOrderByOrdre(c.getId());


                    if (c.getChapitres() == null) {
                        c.setChapitres(new ArrayList<>());
                    } else {
                        c.getChapitres().clear();
                    }
                    c.getChapitres().addAll(chapitres);

                    Cours cours = dozerMapperBean.map(c, Cours.class);
                    cours.setRedacteurId(c.getRedacteur().getId());
                    cours.setChapitres(mapChapitresToDto(chapitres, c.getId()));
                    return cours;
                })
                .collect(Collectors.toList());
    }

    public List<Cours> obtenirCoursParEtat(EtatCours etat) {
        List<CoursEntity> coursEntities = daoAccessorService.getRepository(CoursRepository.class)
                .findByEtat(etat);

        return coursEntities.stream()
                .map(c -> {
                    List<ChapitreEntity> chapitres = daoAccessorService.getRepository(ChapitreRepository.class)
                            .findByCoursIdOrderByOrdre(c.getId());


                    if (c.getChapitres() == null) {
                        c.setChapitres(new ArrayList<>());
                    } else {
                        c.getChapitres().clear();
                    }
                    c.getChapitres().addAll(chapitres);

                    Cours cours = dozerMapperBean.map(c, Cours.class);
                    cours.setRedacteurId(c.getRedacteur().getId());
                    cours.setChapitres(mapChapitresToDto(chapitres, c.getId()));
                    return cours;
                })
                .collect(Collectors.toList());
    }

    public List<Cours> obtenirCoursAccessibles(String userId) {
        List<CoursEntity> coursEntities = daoAccessorService.getRepository(CoursRepository.class)
                .findAccessibleCours(userId);

        return coursEntities.stream()
                .map(c -> {
                    List<ChapitreEntity> chapitres = daoAccessorService.getRepository(ChapitreRepository.class)
                            .findByCoursIdOrderByOrdre(c.getId());

                    // Correction ici
                    if (c.getChapitres() == null) {
                        c.setChapitres(new ArrayList<>());
                    } else {
                        c.getChapitres().clear();
                    }
                    c.getChapitres().addAll(chapitres);

                    Cours cours = dozerMapperBean.map(c, Cours.class);
                    cours.setRedacteurId(c.getRedacteur().getId());
                    cours.setChapitres(mapChapitresToDto(chapitres, c.getId()));
                    return cours;
                })
                .collect(Collectors.toList());
    }

    /** Plafond d'une mesure de temps de lecture envoyée par le client (4 h). */
    public static final long DUREE_LECTURE_MAX_SECONDES = 4 * 3600L;

    /** Durée envoyée par le client ramenée dans [0, 4 h] (null / négative = 0). */
    public static long dureeLectureValide(Long dureeSecondes) {
        if (dureeSecondes == null || dureeSecondes < 0) return 0L;
        return Math.min(dureeSecondes, DUREE_LECTURE_MAX_SECONDES);
    }

    /**
     * Marque un chapitre comme terminé. {@code dureeSecondes} : temps de lecture mesuré par le client depuis
     * l'ouverture du chapitre (ou la complétion précédente) ; plafonné à 4 h et cumulé si le chapitre est
     * de nouveau marqué terminé.
     */
    public void marquerChapitreComplete(String utilisateurId, String chapitreId, Long dureeSecondes) {
        long duree = dureeLectureValide(dureeSecondes);
        ProgressionChapitreRepository repo = daoAccessorService.getRepository(ProgressionChapitreRepository.class);
        ProgressionChapitreId id = new ProgressionChapitreId(utilisateurId, chapitreId);
        Optional<ProgressionChapitreEntity> existante = repo.findById(id);
        if (existante.isPresent()) {
            if (duree > 0) {
                ProgressionChapitreEntity p = existante.get();
                p.setDureeSecondes(p.getDureeSecondes() + duree);
                repo.save(p);
                log.info("Chapitre {} déjà complété par {} : +{} s de lecture", chapitreId, utilisateurId, duree);
            }
            return;
        }

        UtilisateursEntity utilisateur = daoAccessorService.getRepository(UtilisateursRepository.class)
                .findById(utilisateurId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Utilisateur introuvable"));

        ChapitreEntity chapitre = daoAccessorService.getRepository(ChapitreRepository.class)
                .findById(chapitreId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Chapitre introuvable"));

        ProgressionChapitreEntity progression = new ProgressionChapitreEntity();
        progression.setId(id);
        progression.setUtilisateur(utilisateur);
        progression.setChapitre(chapitre);
        progression.setDateCompletion(new Date());
        progression.setDureeSecondes(duree);
        repo.save(progression);
        log.info("Chapitre {} marqué complété par {} ({} s)", chapitreId, utilisateurId, duree);
    }

    public CoursProgressionDTO obtenirProgression(String utilisateurId, String coursId) {
        List<ChapitreEntity> tousChapitres = daoAccessorService.getRepository(ChapitreRepository.class)
                .findByCoursIdOrderByOrdre(coursId);

        Map<String, ProgressionChapitreEntity> completions = daoAccessorService.getRepository(ProgressionChapitreRepository.class)
                .findByUtilisateurIdAndCoursId(utilisateurId, coursId).stream()
                .collect(Collectors.toMap(p -> p.getId().getChapitreId(), p -> p, (a, b) -> a, LinkedHashMap::new));

        List<String> completesIds = new ArrayList<>(completions.keySet());
        long tempsTotal = completions.values().stream().mapToLong(ProgressionChapitreEntity::getDureeSecondes).sum();
        List<CoursProgressionDTO.Chapitre> chapitres = tousChapitres.stream().map(ch -> {
            ProgressionChapitreEntity p = completions.get(ch.getId());
            return CoursProgressionDTO.Chapitre.builder()
                    .chapitreId(ch.getId())
                    .titre(ch.getTitre())
                    .complete(p != null)
                    .dateCompletion(p == null ? null : p.getDateCompletion())
                    .dureeSecondes(p == null ? 0L : p.getDureeSecondes())
                    .build();
        }).collect(Collectors.toList());

        int total = tousChapitres.size();
        int completes = completesIds.size();
        int pourcentage = total == 0 ? 0 : (completes * 100) / total;

        return CoursProgressionDTO.builder()
                .coursId(coursId)
                .utilisateurId(utilisateurId)
                .totalChapitres(total)
                .chapitresCompletes(completes)
                .pourcentage(pourcentage)
                .chapitresCompletesIds(completesIds)
                .tempsTotalSecondes(tempsTotal)
                .chapitres(chapitres)
                .build();
    }

    public Cours obtenirCoursAvecChapitres(String coursId) {
        CoursEntity coursEntity = daoAccessorService.getRepository(CoursRepository.class)
                .findById(coursId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Cours introuvable"));

        // Load chapters
        List<ChapitreEntity> chapitres = daoAccessorService.getRepository(ChapitreRepository.class)
                .findByCoursIdOrderByOrdre(coursId);

        // Correction ici
        if (coursEntity.getChapitres() == null) {
            coursEntity.setChapitres(new ArrayList<>());
        } else {
            coursEntity.getChapitres().clear();
        }
        coursEntity.getChapitres().addAll(chapitres);

        Cours cours = dozerMapperBean.map(coursEntity, Cours.class);
        cours.setRedacteurId(coursEntity.getRedacteur().getId());
        cours.setChapitres(mapChapitresToDto(chapitres, coursId));
        return cours;
    }
}