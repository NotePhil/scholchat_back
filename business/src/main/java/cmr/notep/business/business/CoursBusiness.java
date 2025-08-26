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

@Component
@Slf4j
public class CoursBusiness {
    private final DaoAccessorService daoAccessorService;

    public CoursBusiness(DaoAccessorService daoAccessorService) {
        this.daoAccessorService = daoAccessorService;
    }


    @Transactional
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

                // Set the course reference for the chapter
                chapitreEntity.setCours(entity);

                // Find the subject for this chapter
                MatiereEntity matiereEntity = daoAccessorService.getRepository(MatiereRepository.class)
                        .findById(chapitre.getMatiereId())
                        .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND,
                                "Matière introuvable: " + chapitre.getMatiereId()));
                chapitreEntity.setMatiere(matiereEntity);

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
        result.setChapitres(mapChapitresToDto(savedEntity.getChapitres()));

        return result;
    }

    private List<Chapitre> mapChapitresToDto(List<ChapitreEntity> chapitreEntities) {
        return chapitreEntities.stream()
                .map(c -> {
                    Chapitre chapitre = dozerMapperBean.map(c, Chapitre.class);
                    chapitre.setCoursId(c.getCours() != null ? c.getCours().getId() : null);
                    chapitre.setMatiereId(c.getMatiere() != null ? c.getMatiere().getId() : null);
                    return chapitre;
                })
                .collect(Collectors.toList());
    }
    @Transactional
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

        // Process chapters if provided
        if (cours.getChapitres() != null) {
            // First, remove existing chapters
            daoAccessorService.getRepository(ChapitreRepository.class).deleteByCoursId(coursId);

            List<ChapitreEntity> chapitreEntities = new ArrayList<>();
            StringBuilder contentBuilder = new StringBuilder();

            for (Chapitre chapitre : cours.getChapitres()) {
                ChapitreEntity chapitreEntity = dozerMapperBean.map(chapitre, ChapitreEntity.class);
                chapitreEntity.setCours(existingEntity);

                // Find the subject for this chapter
                MatiereEntity matiereEntity = daoAccessorService.getRepository(MatiereRepository.class)
                        .findById(chapitre.getMatiereId())
                        .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND,
                                "Matière introuvable: " + chapitre.getMatiereId()));
                chapitreEntity.setMatiere(matiereEntity);

                chapitreEntities.add(chapitreEntity);

                // Add to global content
                contentBuilder.append("=== ").append(chapitre.getTitre()).append(" ===\n");
                contentBuilder.append(chapitre.getContenu()).append("\n\n");
            }

            existingEntity.setChapitres(chapitreEntities);
            existingEntity.setContenu(contentBuilder.toString());
        }

        // Save the updated course
        CoursEntity updatedEntity = daoAccessorService.getRepository(CoursRepository.class).save(existingEntity);
        log.info("Updated course entity ID: {}", updatedEntity.getId());

        // Map back to DTO
        Cours result = dozerMapperBean.map(updatedEntity, Cours.class);
        result.setRedacteurId(updatedEntity.getRedacteur().getId());
        result.setChapitres(mapChapitresToDto(updatedEntity.getChapitres()));

        return result;
    }



    @Transactional
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
        coursEntity.setChapitres(chapitres);

        Cours cours = dozerMapperBean.map(coursEntity, Cours.class);
        cours.setRedacteurId(coursEntity.getRedacteur().getId());
        cours.setChapitres(mapChapitresToDto(chapitres));
        return cours;
    }

    public List<Cours> obtenirCoursParRestriction(String restriction) {
        List<CoursEntity> coursEntities = daoAccessorService.getRepository(CoursRepository.class)
                .findByRestriction(restriction);

        return coursEntities.stream()
                .map(c -> {
                    List<ChapitreEntity> chapitres = daoAccessorService.getRepository(ChapitreRepository.class)
                            .findByCoursIdOrderByOrdre(c.getId());
                    c.setChapitres(chapitres);

                    Cours cours = dozerMapperBean.map(c, Cours.class);
                    cours.setRedacteurId(c.getRedacteur().getId());
                    cours.setChapitres(mapChapitresToDto(chapitres));
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
                    c.setChapitres(chapitres);

                    Cours cours = dozerMapperBean.map(c, Cours.class);
                    cours.setRedacteurId(c.getRedacteur().getId());
                    cours.setChapitres(mapChapitresToDto(chapitres));
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
                    c.setChapitres(chapitres);

                    Cours cours = dozerMapperBean.map(c, Cours.class);
                    cours.setRedacteurId(c.getRedacteur().getId());
                    cours.setChapitres(mapChapitresToDto(chapitres));
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
                    c.setChapitres(chapitres);

                    Cours cours = dozerMapperBean.map(c, Cours.class);
                    cours.setRedacteurId(c.getRedacteur().getId());
                    cours.setChapitres(mapChapitresToDto(chapitres));
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
        coursEntity.setChapitres(chapitres);

        Cours cours = dozerMapperBean.map(coursEntity, Cours.class);
        cours.setRedacteurId(coursEntity.getRedacteur().getId());
        cours.setChapitres(mapChapitresToDto(chapitres));
        return cours;
    }
}