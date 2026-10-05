package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.services.NotificationService;
import cmr.notep.interfaces.dto.ParticipationExerciseRequestDTO;
import cmr.notep.interfaces.dto.ParticipationExerciseResponseDTO;
import cmr.notep.modele.EtatSoumission;
import cmr.notep.modele.TypeAssignation;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.ExerciseProgrammerEntity;
import cmr.notep.ressourcesjpa.dao.ParticiperExoEntity;
import cmr.notep.ressourcesjpa.dao.UtilisateursEntity;
import cmr.notep.ressourcesjpa.dao.ParticiperExoId;
import cmr.notep.ressourcesjpa.repository.ExerciseProgrammerRepository;
import cmr.notep.ressourcesjpa.repository.RepondreRepository;
import cmr.notep.ressourcesjpa.dao.ChoixReponseEntity;
import cmr.notep.ressourcesjpa.dao.QuestionReponseEntity;
import cmr.notep.ressourcesjpa.dao.RepondreEntity;
import cmr.notep.modele.TypeQuestion;
import cmr.notep.ressourcesjpa.repository.ParticiperExoRepository;
import cmr.notep.ressourcesjpa.repository.UtilisateursRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class ParticipationExerciseBusiness {

    private final DaoAccessorService daoAccessorService;
    private final NotificationService notificationService;

    public ParticipationExerciseResponseDTO participerAExercise(ParticipationExerciseRequestDTO requestDTO) {
        log.info("Participation de l'utilisateur {} à l'exercice programmé {}",
                requestDTO.getUtilisateurId(), requestDTO.getExerciseProgrammerId());

        // Vérifier l'existence de l'utilisateur
        UtilisateursEntity utilisateur = daoAccessorService.getRepository(UtilisateursRepository.class)
                .findById(requestDTO.getUtilisateurId())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Utilisateur introuvable"));

        // Vérifier l'existence de l'exercice programmé
        ExerciseProgrammerEntity exerciseProgrammer = daoAccessorService.getRepository(ExerciseProgrammerRepository.class)
                .findById(requestDTO.getExerciseProgrammerId())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Exercice programmé introuvable"));

        // Vérifier si la participation existe déjà
        ParticiperExoRepository repository = daoAccessorService.getRepository(ParticiperExoRepository.class);
        if (repository.existsByUtilisateurIdAndExerciseProgrammerId(
                requestDTO.getUtilisateurId(), requestDTO.getExerciseProgrammerId())) {
            throw new SchoolException(SchoolErrorCode.ALREADY_EXISTS,
                    "L'utilisateur participe déjà à cet exercice");
        }

        // Créer la participation
        ParticiperExoEntity participation = new ParticiperExoEntity();
        participation.setId(new ParticiperExoId(requestDTO.getUtilisateurId(), requestDTO.getExerciseProgrammerId()));
        participation.setUtilisateur(utilisateur);
        participation.setExerciseProgrammer(exerciseProgrammer);
        participation.setDateDebut(requestDTO.getDateDebut() != null ? requestDTO.getDateDebut() : new Date());
        participation.setDateFin(requestDTO.getDateFin());
        participation.setNote(requestDTO.getNote());
        participation.setAppreciation(requestDTO.getAppreciation());
        participation.setEtatSoumission(requestDTO.getEtatSoumission() != null 
            ? requestDTO.getEtatSoumission() 
            : cmr.notep.modele.EtatSoumission.EN_COURS);
        if (estUneSoumission(participation.getEtatSoumission())) {
            autoCorrigerQuestionsObjectives(requestDTO.getUtilisateurId(), exerciseProgrammer);
            participation.setEtatSoumission(EtatSoumission.EN_ATTENTE_CORRECTION);
        }
        participation.setDateSoumission(new Date());

        ParticiperExoEntity savedParticipation = repository.save(participation);

        log.info("Participation créée avec succès");
        return mapToResponseDTO(savedParticipation);
    }

    public ParticipationExerciseResponseDTO mettreAJourParticipation(ParticipationExerciseRequestDTO requestDTO) {
        log.info("Mise à jour de la participation de l'utilisateur {} à l'exercice programmé {}",
                requestDTO.getUtilisateurId(), requestDTO.getExerciseProgrammerId());

        ParticiperExoRepository repository = daoAccessorService.getRepository(ParticiperExoRepository.class);
        ParticiperExoEntity participation = repository
                .findByUtilisateurIdAndExerciseProgrammerId(
                        requestDTO.getUtilisateurId(), requestDTO.getExerciseProgrammerId())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Participation introuvable"));

        // Mettre à jour les champs
        if (requestDTO.getDateDebut() != null) {
            participation.setDateDebut(requestDTO.getDateDebut());
        }
        if (requestDTO.getDateFin() != null) {
            participation.setDateFin(requestDTO.getDateFin());
        }
        if (requestDTO.getNote() != null) {
            participation.setNote(requestDTO.getNote());
        }
        if (requestDTO.getAppreciation() != null) {
            participation.setAppreciation(requestDTO.getAppreciation());
        }
        boolean soumission = estUneSoumission(requestDTO.getEtatSoumission());
        if (soumission) {
            // L'élève rend sa copie (le web/mobile envoient SOUMIS) : les QCM / Vrai-Faux sont corrigés
            // automatiquement et la copie part dans la liste « à corriger » du professeur.
            autoCorrigerQuestionsObjectives(requestDTO.getUtilisateurId(), participation.getExerciseProgrammer());
            participation.setEtatSoumission(EtatSoumission.EN_ATTENTE_CORRECTION);
        } else if (requestDTO.getEtatSoumission() != null) {
            participation.setEtatSoumission(requestDTO.getEtatSoumission());
        }

        ParticiperExoEntity updatedParticipation = repository.save(participation);

        // Notify professor when student submits a DEVOIR
        if (soumission) {
            try {
                ExerciseProgrammerEntity prog = updatedParticipation.getExerciseProgrammer();
                if (TypeAssignation.DEVOIR.equals(prog.getTypeAssignation())) {
                    String studentName = updatedParticipation.getUtilisateur().getPrenom()
                            + " " + updatedParticipation.getUtilisateur().getNom();
                    notificationService.createDevoirSoumisNotification(
                            prog.getId(),
                            prog.getExercise().getNom(),
                            updatedParticipation.getUtilisateur().getId(),
                            studentName,
                            prog.getProgrammePar().getId());
                }
            } catch (Exception e) {
                log.error("Failed to send devoir soumis notification: {}", e.getMessage());
            }
        }

        log.info("Participation mise à jour avec succès");
        return mapToResponseDTO(updatedParticipation);
    }

    public List<ParticipationExerciseResponseDTO> obtenirParticipationsParUtilisateur(String utilisateurId) {
        log.info("Récupération des participations de l'utilisateur: {}", utilisateurId);

        ParticiperExoRepository repository = daoAccessorService.getRepository(ParticiperExoRepository.class);
        return repository.findByUtilisateurId(utilisateurId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ParticipationExerciseResponseDTO> obtenirParticipationsParExercise(String exerciseProgrammerId) {
        log.info("Récupération des participations à l'exercice programmé: {}", exerciseProgrammerId);
        return daoAccessorService.getRepository(ParticiperExoRepository.class)
                .findByExerciseProgrammerId(exerciseProgrammerId)
                .stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    public List<ParticipationExerciseResponseDTO> obtenirParticipationsEnAttenteCorrection(String exerciseProgrammerId) {
        log.info("Récupération des participations EN_ATTENTE_CORRECTION pour l'exercice: {}", exerciseProgrammerId);
        return daoAccessorService.getRepository(ParticiperExoRepository.class)
                .findByExerciseProgrammerId(exerciseProgrammerId)
                .stream()
                .filter(p -> aCorriger(p.getEtatSoumission()))
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ParticipationExerciseResponseDTO> obtenirToutesParticipationsACorriger(String professeurId) {
        log.info("Récupération de toutes les participations à corriger pour le professeur: {}", professeurId);
        return daoAccessorService.getRepository(ParticiperExoRepository.class)
                .findAll()
                .stream()
                .filter(p -> aCorriger(p.getEtatSoumission())
                    && p.getExerciseProgrammer().getProgrammePar().getId().equals(professeurId))
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public void supprimerParticipation(String utilisateurId, String exerciseProgrammerId) {
        log.info("Suppression de la participation de l'utilisateur {} à l'exercice {}",
                utilisateurId, exerciseProgrammerId);

        ParticiperExoRepository repository = daoAccessorService.getRepository(ParticiperExoRepository.class);
        ParticiperExoEntity participation = repository
                .findByUtilisateurIdAndExerciseProgrammerId(utilisateurId, exerciseProgrammerId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Participation introuvable"));

        repository.delete(participation);
        log.info("Participation supprimée avec succès");
    }

    private static boolean estUneSoumission(EtatSoumission etat) {
        return EtatSoumission.SOUMIS.equals(etat) || EtatSoumission.EN_ATTENTE_CORRECTION.equals(etat);
    }

    /** Copies rendues et pas encore corrigées (les anciennes copies SOUMIS comprises). */
    private static boolean aCorriger(EtatSoumission etat) {
        return estUneSoumission(etat);
    }

    private static String normaliser(String v) {
        if (v == null) return "";
        String n = java.text.Normalizer.normalize(v.trim().toLowerCase(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        if (n.equals("true") || n.equals("vrai")) return "vrai";
        if (n.equals("false") || n.equals("faux")) return "faux";
        return n;
    }

    /**
     * Corrige les questions à réponse fermée (QCM, Vrai/Faux) encore non corrigées : la réponse
     * (texte du choix, ou son id) est comparée au(x) choix correct(s), à défaut à la réponse attendue
     * de la question. Pose estCorrecte et la note « obtenus/max » ; le professeur peut ensuite corriger.
     */
    private void autoCorrigerQuestionsObjectives(String utilisateurId, ExerciseProgrammerEntity prog) {
        try {
            if (prog == null || prog.getExercise() == null) return;
            RepondreRepository repondreRepository = daoAccessorService.getRepository(RepondreRepository.class);
            for (RepondreEntity r : repondreRepository.findByUtilisateurIdAndExerciseId(utilisateurId, prog.getExercise().getId())) {
                QuestionReponseEntity q = r.getQuestion();
                if (q == null || r.getEstCorrecte() != null) continue;
                if (q.getTypeQuestion() != TypeQuestion.QCM && q.getTypeQuestion() != TypeQuestion.VRAI_FAUX) continue;
                java.util.Set<String> attendues = new java.util.HashSet<>();
                if (q.getChoixReponses() != null) {
                    for (ChoixReponseEntity c : q.getChoixReponses()) {
                        if (Boolean.TRUE.equals(c.getEstCorrect())) {
                            attendues.add(normaliser(c.getTexte()));
                            if (c.getId() != null) attendues.add(normaliser(c.getId()));
                        }
                    }
                }
                if (attendues.isEmpty() && q.getReponse() != null && !q.getReponse().isBlank()) {
                    attendues.add(normaliser(q.getReponse()));
                }
                if (attendues.isEmpty()) continue; // pas de corrigé : correction manuelle
                boolean correcte = attendues.contains(normaliser(r.getReponseUtilisateur()));
                int max = q.getPoints() != null && q.getPoints() > 0 ? q.getPoints() : 1;
                r.setEstCorrecte(correcte);
                if (r.getNote() == null || r.getNote().isBlank()) r.setNote((correcte ? max : 0) + "/" + max);
                repondreRepository.save(r);
            }
        } catch (Exception e) {
            log.warn("Correction automatique impossible pour {} : {}", utilisateurId, e.getMessage());
        }
    }

    private ParticipationExerciseResponseDTO mapToResponseDTO(ParticiperExoEntity participation) {
        return ParticipationExerciseResponseDTO.builder()
                .utilisateurId(participation.getUtilisateur().getId())
                .utilisateurNom(participation.getUtilisateur().getNom())
                .utilisateurPrenom(participation.getUtilisateur().getPrenom())
                .exerciseProgrammerId(participation.getExerciseProgrammer().getId())
                .exerciseProgrammerNom(participation.getExerciseProgrammer().getExercise().getNom())
                .etatSoumission(participation.getEtatSoumission())
                .note(participation.getNote())
                .appreciation(participation.getAppreciation())
                .dateDebut(participation.getDateDebut())
                .dateFin(participation.getDateFin())
                .dateSoumission(participation.getDateSoumission())
                .build();
    }
}