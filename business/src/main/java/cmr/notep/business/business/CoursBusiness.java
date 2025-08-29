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
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

                // Ensure ID is null for new chapters to avoid the "id must not be null" error
                chapitreEntity.setId(null);

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
            existingEntity.setReferences(cours.getReferences());
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

        // Process chapters if provided - FIXED: Proper orphan removal handling
        if (cours.getChapitres() != null) {
            // Clear existing chapters and let orphan removal handle deletion
            existingEntity.getChapitres().clear();

            List<ChapitreEntity> chapitreEntities = new ArrayList<>();
            StringBuilder contentBuilder = new StringBuilder();

            for (Chapitre chapitre : cours.getChapitres()) {
                ChapitreEntity chapitreEntity = dozerMapperBean.map(chapitre, ChapitreEntity.class);
                chapitreEntity.setCours(existingEntity);
                chapitreEntities.add(chapitreEntity);

                // Add to global content
                contentBuilder.append("=== ").append(chapitre.getTitre()).append(" ===\n");
                contentBuilder.append(chapitre.getContenu()).append("\n\n");
            }

            // Add all new chapters to the existing collection
            existingEntity.getChapitres().addAll(chapitreEntities);
            existingEntity.setContenu(contentBuilder.toString());
        }

        // Save the updated course
        CoursEntity updatedEntity = daoAccessorService.getRepository(CoursRepository.class).save(existingEntity);
        log.info("Updated course entity ID: {}", updatedEntity.getId());

        // Map back to DTO
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

        // Delete the course (cascade should handle chapters)
        daoAccessorService.getRepository(CoursRepository.class).delete(coursEntity);
        log.info("Deleted course ID: {}", coursId);
    }

    public Cours obtenirCoursParId(String coursId) {
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

    public List<Cours> obtenirCoursParRestriction(String restriction) {
        List<CoursEntity> coursEntities = daoAccessorService.getRepository(CoursRepository.class)
                .findByRestriction(restriction);

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

    public List<Cours> obtenirCoursParProfesseur(String professeurId) {
        List<CoursEntity> coursEntities = daoAccessorService.getRepository(CoursRepository.class)
                .findByRedacteurId(professeurId);

        return coursEntities.stream()
                .map(c -> {
                    // Charger explicitement les chapitres pour chaque cours
                    List<ChapitreEntity> chapitres = daoAccessorService.getRepository(ChapitreRepository.class)
                            .findByCoursIdOrderByOrdre(c.getId());

                    // NE PAS FAIRE: c.setChapitres(chapitres); ← Ça cause l'erreur
                    // À la place, utilisez la collection existante
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