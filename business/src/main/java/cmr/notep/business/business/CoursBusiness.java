package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Transactional
@Component
@Slf4j
public class CoursBusiness {
    private final DaoAccessorService daoAccessorService;

    public CoursBusiness(DaoAccessorService daoAccessorService) {
        this.daoAccessorService = daoAccessorService;
    }


    public Cours creerCours(Cours cours) {
        // Validate professor
        ProfesseursEntity professeur = daoAccessorService.getRepository(ProfesseursRepository.class)
                .findById(cours.getRedacteurId())
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
            ProfesseursEntity professeur = daoAccessorService.getRepository(ProfesseursRepository.class)
                    .findById(cours.getRedacteurId())
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
            existingEntity.setEtat(cours.getEtat());
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

            existingEntity.getChapitres().clear();

            List<ChapitreEntity> chapitreEntities = new ArrayList<>();
            StringBuilder contentBuilder = new StringBuilder();

            for (Chapitre chapitre : cours.getChapitres()) {
                ChapitreEntity chapitreEntity = dozerMapperBean.map(chapitre, ChapitreEntity.class);
                // Generate UUID for new chapters
                if (chapitreEntity.getId() == null) {
                    chapitreEntity.setId(UUID.randomUUID().toString());
                }
                chapitreEntity.setCours(existingEntity);
                chapitreEntities.add(chapitreEntity);

                contentBuilder.append("=== ").append(chapitre.getTitre()).append(" ===\n");
                contentBuilder.append(chapitre.getContenu()).append("\n\n");
            }


            existingEntity.getChapitres().addAll(chapitreEntities);
            existingEntity.setContenu(contentBuilder.toString());
        }

        // Save the updated course
        CoursEntity updatedEntity = daoAccessorService.getRepository(CoursRepository.class).save(existingEntity);
        log.info("Updated course entity ID: {}", updatedEntity.getId());


        Cours result = dozerMapperBean.map(updatedEntity, Cours.class);
        result.setRedacteurId(updatedEntity.getRedacteur().getId());
        result.setChapitres(mapChapitresToDto(updatedEntity.getChapitres(), updatedEntity.getId()));

        return result;
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