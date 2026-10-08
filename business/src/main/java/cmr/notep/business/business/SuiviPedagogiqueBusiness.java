package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.dto.ClasseResumeEleveDTO;
import cmr.notep.interfaces.dto.CoursProgrammeResumeDTO;
import cmr.notep.interfaces.dto.ExerciceCoursClasseDTO;
import cmr.notep.interfaces.dto.ProgressionEleveDTO;
import cmr.notep.interfaces.dto.StatistiquesClasseDTO;
import cmr.notep.modele.EtatExercise;
import cmr.notep.modele.EtatSoumission;
import cmr.notep.modele.TypeAssignation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Lectures agrégées du suivi pédagogique (résumé des cours d'une classe, exercices d'un cours, cartes de classes de
 * l'élève, progression de l'élève, statistiques de la classe).
 *
 * Chaque lecture exécute un nombre CONSTANT de requêtes SQL ensemblistes (IN (...) / GROUP BY), quel que soit le
 * nombre d'élèves, de cours ou d'exercices : pas de N+1. Les agrégats sont ensuite calculés en mémoire.
 *
 * Règles :
 * <ul>
 *   <li>Élèves d'une classe : comptes ayant accès (acceder) avec un profil élève, hors enseignants de la classe.</li>
 *   <li>Cours d'une classe : cours programmés dans la classe (cours_programmer, table de jointure ou ancienne colonne
 *       classe_id) ∪ cours auxquels sont rattachés les exercices programmés de la classe.</li>
 *   <li>Tout exercice programmé appartient à un cours : les anciennes lignes sans cours (cours_id NULL) sont
 *       ignorées (ni listées, ni comptées).</li>
 *   <li>Exercices programmés à l'état ANNULE : ignorés dans les compteurs et moyennes.</li>
 *   <li>Copie rendue : SOUMIS, EN_ATTENTE_CORRECTION ou CORRIGE. Moyennes : copies CORRIGE dont la note est lisible,
 *       ramenée sur 20 ({@link #noteSur20(String)}).</li>
 *   <li>Chapitre lu : ligne dans progression_chapitre, ou chapitre_progress.completed = true.</li>
 * </ul>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SuiviPedagogiqueBusiness {

    private static final Set<String> ETATS_RENDUS = Set.of(
            EtatSoumission.SOUMIS.name(), EtatSoumission.EN_ATTENTE_CORRECTION.name(), EtatSoumission.CORRIGE.name());
    private static final Set<String> ETATS_A_CORRIGER = Set.of(
            EtatSoumission.SOUMIS.name(), EtatSoumission.EN_ATTENTE_CORRECTION.name());

    private final NamedParameterJdbcTemplate jdbc;

    // ─── Lignes lues ──────────────────────────────────────────────────────────

    record Exo(String classeId, String id, String exerciseId, String titre, String type, String etat,
               Timestamp prevue, Timestamp debut, Timestamp fin, String coursId, String coursTitre) {
        boolean annule() { return EtatExercise.ANNULE.name().equals(etat); }
        boolean devoir() { return TypeAssignation.DEVOIR.name().equals(type); }
        boolean echu(long now) { return fin != null && fin.getTime() < now; }
    }

    record CoursProg(String classeId, String coursId, String titre, int nbSessions, Timestamp prochaine) {}

    record Part(String userId, String epId, String etat, String note, Timestamp dateSoumission) {
        boolean rendu() { return etat != null && ETATS_RENDUS.contains(etat); }
        boolean corrige() { return EtatSoumission.CORRIGE.name().equals(etat); }
        Double sur20() { return corrige() ? noteSur20(note) : null; }
    }

    record Eleve(String id, String nom, String prenom) {}

    record Lecture(int lus, Timestamp derniere) {}

    record Classe(String id, String nom, String niveau, String etat) {}

    // ─── Endpoints ────────────────────────────────────────────────────────────

    public List<CoursProgrammeResumeDTO> resumeCoursProgrammes(String classeId) {
        List<String> classes = List.of(classeId);
        List<Exo> exos = actifs(exercices(classes));
        List<CoursProg> cps = coursProgrammes(classes);
        LinkedHashMap<String, String> cours = coursDeLaClasse(classeId, cps, exos);
        Map<String, Integer> chapitres = chapitresTotal(cours.keySet());
        Map<String, List<String>> matieres = matieres(cours.keySet());
        Map<String, CoursProg> cpParCours = cps.stream()
                .collect(Collectors.toMap(CoursProg::coursId, Function.identity(), (a, b) -> a));

        List<CoursProgrammeResumeDTO> out = new ArrayList<>();
        cours.forEach((coursId, titre) -> {
            CoursProg cp = cpParCours.get(coursId);
            List<Exo> duCours = exos.stream().filter(e -> coursId.equals(e.coursId())).toList();
            List<String> noms = matieres.getOrDefault(coursId, List.of());
            out.add(CoursProgrammeResumeDTO.builder()
                    .coursId(coursId)
                    .titre(titre)
                    .matieres(noms)
                    .matiere(noms.isEmpty() ? null : String.join(", ", noms))
                    .nbChapitres(chapitres.getOrDefault(coursId, 0))
                    .nbSessions(cp == null ? 0 : cp.nbSessions())
                    .prochaineSession(cp == null || cp.prochaine() == null ? null : cp.prochaine().toLocalDateTime())
                    .nbExercices((int) duCours.stream().filter(e -> !e.devoir()).count())
                    .nbDevoirs((int) duCours.stream().filter(Exo::devoir).count())
                    .build());
        });
        return out;
    }

    public List<ExerciceCoursClasseDTO> exercicesDuCours(String classeId, String coursId, String eleveId) {
        if (coursId == null || coursId.isBlank()) return List.of();
        List<Exo> exos = dedoublonner(exercices(List.of(classeId))).stream()
                .filter(e -> coursId.equals(e.coursId()))
                .sorted(Comparator.comparing(Exo::prevue, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        if (exos.isEmpty()) return List.of();

        Map<String, int[]> questions = questions(exos.stream().map(Exo::exerciseId).collect(Collectors.toSet()));
        Map<String, Part> parts = eleveId == null ? Map.of()
                : participations(ids(exos), List.of(eleveId)).stream()
                        .collect(Collectors.toMap(Part::epId, Function.identity(), (a, b) -> a));
        long now = System.currentTimeMillis();

        List<ExerciceCoursClasseDTO> out = new ArrayList<>();
        for (Exo e : exos) {
            int[] q = questions.getOrDefault(e.exerciseId(), new int[]{0, 0});
            ExerciceCoursClasseDTO.ExerciceCoursClasseDTOBuilder b = ExerciceCoursClasseDTO.builder()
                    .exerciseProgrammerId(e.id())
                    .exerciseId(e.exerciseId())
                    .titre(e.titre())
                    .coursId(e.coursId())
                    .coursTitre(e.coursTitre())
                    .typeAssignation(enumOrNull(TypeAssignation.class, e.type()))
                    .etat(enumOrNull(EtatExercise.class, e.etat()))
                    .dateExoPrevue(e.prevue())
                    .dateDebutExoEffectif(e.debut())
                    .dateFinExoEffectif(e.fin())
                    .nbQuestions(q[0])
                    .points(q[1]);
            if (eleveId != null) {
                Part p = parts.get(e.id());
                b.eleveId(eleveId)
                        .etatSoumission(p == null ? null : enumOrNull(EtatSoumission.class, p.etat()))
                        .note(p == null ? null : p.note())
                        .noteSur20(p == null ? null : round2(p.sur20()))
                        .dateSoumission(p == null ? null : p.dateSoumission())
                        .statut(statut(e, p, now));
            }
            out.add(b.build());
        }
        return out;
    }

    public List<ClasseResumeEleveDTO> resumeClassesEleve(String eleveId) {
        List<Classe> classes = classesDeLEleve(eleveId);
        if (classes.isEmpty()) return List.of();
        List<String> classeIds = classes.stream().map(Classe::id).toList();
        List<Exo> exos = actifs(exercices(classeIds));
        List<CoursProg> cps = coursProgrammes(classeIds);
        Map<String, Part> parts = participations(ids(exos), List.of(eleveId)).stream()
                .collect(Collectors.toMap(Part::epId, Function.identity(), (a, b) -> a));
        long now = System.currentTimeMillis();

        List<ClasseResumeEleveDTO> out = new ArrayList<>();
        for (Classe c : classes) {
            List<Exo> exosClasse = exos.stream().filter(e -> c.id().equals(e.classeId())).toList();
            Set<String> cours = new HashSet<>();
            cps.stream().filter(cp -> c.id().equals(cp.classeId())).forEach(cp -> cours.add(cp.coursId()));
            exosClasse.stream().filter(e -> e.coursId() != null).forEach(e -> cours.add(e.coursId()));
            int aFaire = 0, enRetard = 0;
            List<Double> notes = new ArrayList<>();
            for (Exo e : exosClasse) {
                Part p = parts.get(e.id());
                if (e.devoir() && (p == null || !p.rendu())) {
                    if (e.echu(now)) enRetard++; else aFaire++;
                }
                if (p != null && p.sur20() != null) notes.add(p.sur20());
            }
            out.add(ClasseResumeEleveDTO.builder()
                    .classeId(c.id()).nom(c.nom()).niveau(c.niveau()).etat(c.etat())
                    .nbCours(cours.size())
                    .nbDevoirsAFaire(aFaire)
                    .nbDevoirsEnRetard(enRetard)
                    .moyenne(round2(moyenne(notes)))
                    .build());
        }
        return out;
    }

    public ProgressionEleveDTO progressionEleve(String eleveId, String classeId) {
        List<Classe> classes = classesDeLEleve(eleveId);
        if (classeId != null) {
            classes = classes.stream().filter(c -> c.id().equals(classeId)).toList();
            if (classes.isEmpty()) {
                throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Cet élève n'est pas inscrit dans cette classe");
            }
        }
        Map<String, String> nomsClasses = classes.stream()
                .collect(Collectors.toMap(Classe::id, Classe::nom, (a, b) -> a));
        List<String> classeIds = classes.stream().map(Classe::id).toList();
        List<Exo> exos = dedoublonner(actifs(exercices(classeIds)));
        List<CoursProg> cps = coursProgrammes(classeIds);

        LinkedHashMap<String, String> cours = new LinkedHashMap<>();
        cps.stream().sorted(Comparator.comparing(CoursProg::titre, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .forEach(cp -> cours.putIfAbsent(cp.coursId(), cp.titre()));
        exos.stream().filter(e -> e.coursId() != null).forEach(e -> cours.putIfAbsent(e.coursId(), e.coursTitre()));

        Map<String, Integer> totalChapitres = chapitresTotal(cours.keySet());
        Map<String, Lecture> lus = chapitresLus(List.of(eleveId), cours.keySet()).getOrDefault(eleveId, Map.of());
        Map<String, Part> parts = participations(ids(exos), List.of(eleveId)).stream()
                .collect(Collectors.toMap(Part::epId, Function.identity(), (a, b) -> a));
        long now = System.currentTimeMillis();

        List<ProgressionEleveDTO.Cours> lignes = new ArrayList<>();
        int sommeLus = 0, sommeTotal = 0;
        Timestamp activiteGlobale = null;
        for (Map.Entry<String, String> c : cours.entrySet()) {
            int total = totalChapitres.getOrDefault(c.getKey(), 0);
            Lecture l = lus.get(c.getKey());
            int nbLus = l == null ? 0 : Math.min(l.lus(), total);
            sommeLus += nbLus;
            sommeTotal += total;
            List<Exo> duCours = exos.stream().filter(e -> c.getKey().equals(e.coursId())).toList();
            ProgressionEleveDTO.Cours ligne = ligneProgression(c.getKey(), c.getValue(), nbLus, total, duCours, parts,
                    l == null ? null : l.derniere());
            lignes.add(ligne);
        }
        for (ProgressionEleveDTO.Cours ligne : lignes) {
            activiteGlobale = max(activiteGlobale, ligne.getDerniereActivite() == null ? null
                    : Timestamp.valueOf(ligne.getDerniereActivite()));
        }

        int devoirsTotal = 0, devoirsRendus = 0;
        List<Double> notes = new ArrayList<>();
        List<ProgressionEleveDTO.DevoirEnRetard> enRetard = new ArrayList<>();
        for (Exo e : exos) {
            Part p = parts.get(e.id());
            if (p != null && p.sur20() != null) notes.add(p.sur20());
            if (!e.devoir()) continue;
            devoirsTotal++;
            if (p != null && p.rendu()) {
                devoirsRendus++;
            } else if (e.echu(now)) {
                enRetard.add(ProgressionEleveDTO.DevoirEnRetard.builder()
                        .exerciseProgrammerId(e.id()).titre(e.titre())
                        .typeAssignation(enumOrNull(TypeAssignation.class, e.type()))
                        .coursId(e.coursId()).coursTitre(e.coursTitre())
                        .classeId(e.classeId()).classeNom(nomsClasses.get(e.classeId()))
                        .dateFinExoEffectif(e.fin())
                        .build());
            }
        }
        enRetard.sort(Comparator.comparing(ProgressionEleveDTO.DevoirEnRetard::getDateFinExoEffectif,
                Comparator.nullsLast(Comparator.naturalOrder())));

        LocalDateTime derniere = activiteGlobale == null ? null : activiteGlobale.toLocalDateTime();
        return ProgressionEleveDTO.builder()
                .eleveId(eleveId)
                .classeId(classeId)
                .global(ProgressionEleveDTO.Global.builder()
                        .progressionCours(sommeTotal == 0 ? 0 : (int) Math.round(sommeLus * 100.0 / sommeTotal))
                        .devoirsRendus(devoirsRendus)
                        .devoirsTotal(devoirsTotal)
                        .moyenne(round2(moyenne(notes)))
                        .dernierActivite(derniere)
                        .derniereActivite(derniere)
                        .build())
                .cours(lignes)
                .devoirsEnRetard(enRetard)
                .build();
    }

    private ProgressionEleveDTO.Cours ligneProgression(String coursId, String titre, int lus, int total,
                                                       List<Exo> exos, Map<String, Part> parts, Timestamp lecture) {
        int faits = 0;
        List<Double> notes = new ArrayList<>();
        Timestamp activite = lecture;
        for (Exo e : exos) {
            Part p = parts.get(e.id());
            if (p == null) continue;
            if (p.rendu()) {
                faits++;
                activite = max(activite, p.dateSoumission());
            }
            if (p.sur20() != null) notes.add(p.sur20());
        }
        return ProgressionEleveDTO.Cours.builder()
                .coursId(coursId).titre(titre)
                .chapitresLus(lus).chapitresTotal(total)
                .pourcentage(total == 0 ? 0 : (int) Math.round(lus * 100.0 / total))
                .exercicesFaits(faits).exercicesTotal(exos.size())
                .moyenne(round2(moyenne(notes)))
                .derniereActivite(activite == null ? null : activite.toLocalDateTime())
                .build();
    }

    public StatistiquesClasseDTO statistiquesClasse(String classeId) {
        List<String> classes = List.of(classeId);
        List<Eleve> eleves = elevesDeLaClasse(classeId);
        List<String> eleveIds = eleves.stream().map(Eleve::id).toList();
        List<Exo> exos = actifs(exercices(classes));
        List<CoursProg> cps = coursProgrammes(classes);
        LinkedHashMap<String, String> cours = coursDeLaClasse(classeId, cps, exos);
        Map<String, Integer> totalChapitres = chapitresTotal(cours.keySet());
        Map<String, Map<String, Lecture>> lus = chapitresLus(eleveIds, cours.keySet());
        // participations des élèves de la classe seulement, indexées par exercice puis par élève
        Map<String, Map<String, Part>> parts = new HashMap<>();
        for (Part p : participations(ids(exos), eleveIds)) {
            parts.computeIfAbsent(p.epId(), k -> new HashMap<>()).put(p.userId(), p);
        }
        long now = System.currentTimeMillis();
        int effectif = eleves.size();

        List<StatistiquesClasseDTO.Cours> lignesCours = new ArrayList<>();
        for (Map.Entry<String, String> c : cours.entrySet()) {
            int total = totalChapitres.getOrDefault(c.getKey(), 0);
            Double progression = null;
            if (total > 0 && effectif > 0) {
                double somme = 0;
                for (String eleveId : eleveIds) {
                    Lecture l = lus.getOrDefault(eleveId, Map.of()).get(c.getKey());
                    somme += (l == null ? 0 : Math.min(l.lus(), total)) * 100.0 / total;
                }
                progression = round1(somme / effectif);
            }
            List<Exo> duCours = exos.stream().filter(e -> c.getKey().equals(e.coursId())).toList();
            lignesCours.add(StatistiquesClasseDTO.Cours.builder()
                    .coursId(c.getKey()).titre(c.getValue()).chapitresTotal(total)
                    .progressionMoyenne(progression)
                    .exercices(statsExercices(duCours, parts, effectif))
                    .build());
        }

        List<StatistiquesClasseDTO.Eleve> lignesEleves = new ArrayList<>();
        for (Eleve el : eleves) {
            Map<String, Lecture> lecture = lus.getOrDefault(el.id(), Map.of());
            List<Double> pourcentages = new ArrayList<>();
            for (String coursId : cours.keySet()) {
                int total = totalChapitres.getOrDefault(coursId, 0);
                if (total == 0) continue;
                Lecture l = lecture.get(coursId);
                pourcentages.add((l == null ? 0 : Math.min(l.lus(), total)) * 100.0 / total);
            }
            int devoirsTotal = 0, devoirsRendus = 0, enRetard = 0;
            List<Double> notes = new ArrayList<>();
            for (Exo e : exos) {
                Part p = parts.getOrDefault(e.id(), Map.of()).get(el.id());
                if (p != null && p.sur20() != null) notes.add(p.sur20());
                if (!e.devoir()) continue;
                devoirsTotal++;
                if (p != null && p.rendu()) devoirsRendus++;
                else if (e.echu(now)) enRetard++;
            }
            lignesEleves.add(StatistiquesClasseDTO.Eleve.builder()
                    .eleveId(el.id()).nom(el.nom()).prenom(el.prenom())
                    .progressionMoyenne(round1(moyenne(pourcentages)))
                    .devoirsRendus(devoirsRendus).devoirsTotal(devoirsTotal)
                    .moyenne(round2(moyenne(notes)))
                    .enRetard(enRetard)
                    .build());
        }

        return StatistiquesClasseDTO.builder()
                .classeId(classeId)
                .effectif(effectif)
                .cours(lignesCours)
                .eleves(lignesEleves)
                .build();
    }

    private List<StatistiquesClasseDTO.Exercice> statsExercices(List<Exo> exos, Map<String, Map<String, Part>> parts,
                                                                int effectif) {
        List<StatistiquesClasseDTO.Exercice> out = new ArrayList<>();
        for (Exo e : exos.stream()
                .sorted(Comparator.comparing(Exo::prevue, Comparator.nullsLast(Comparator.naturalOrder()))).toList()) {
            int rendus = 0, aCorriger = 0, corriges = 0;
            List<Double> notes = new ArrayList<>();
            for (Part p : parts.getOrDefault(e.id(), Map.of()).values()) {
                if (p.rendu()) rendus++;
                if (p.etat() != null && ETATS_A_CORRIGER.contains(p.etat())) aCorriger++;
                if (p.corrige()) corriges++;
                if (p.sur20() != null) notes.add(p.sur20());
            }
            out.add(StatistiquesClasseDTO.Exercice.builder()
                    .exerciseProgrammerId(e.id()).titre(e.titre())
                    .typeAssignation(enumOrNull(TypeAssignation.class, e.type()))
                    .dateFinExoEffectif(e.fin())
                    .rendus(rendus).attendus(effectif).enAttenteCorrection(aCorriger).corriges(corriges)
                    .moyenne(round2(moyenne(notes)))
                    .min(notes.isEmpty() ? null : round2(Collections.min(notes)))
                    .max(notes.isEmpty() ? null : round2(Collections.max(notes)))
                    .build());
        }
        return out;
    }

    // ─── Requêtes (ensemblistes) ──────────────────────────────────────────────

    private static final String SQL_EXERCICES = """
            SELECT epc.classe_id, ep.id, ep.source_exercise_id, ex.nom, ep.type_assignation, ep.etat_exercise_programmer,
                   ep.date_exo_prevue, ep.date_debut_exo_effectif, ep.date_fin_exo_effectif, ep.cours_id, c.titre
            FROM ressources.exercise_programmer_classes epc
            JOIN ressources.exercises_programmer ep ON ep.id = epc.exercise_programmer_id
            JOIN ressources.exercises ex ON ex.id = ep.source_exercise_id
            JOIN ressources.cours c ON c.id = ep.cours_id
            WHERE epc.classe_id IN (:classeIds)
            """;

    List<Exo> exercices(Collection<String> classeIds) {
        if (classeIds.isEmpty()) return List.of();
        return jdbc.query(SQL_EXERCICES, new MapSqlParameterSource("classeIds", classeIds), (rs, i) -> new Exo(
                rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6),
                rs.getTimestamp(7), rs.getTimestamp(8), rs.getTimestamp(9), rs.getString(10), rs.getString(11)));
    }

    /** Programmations de cours par classe : table de jointure cours_programmer_classes ou ancienne colonne classe_id. */
    private static final String SQL_COURS_PROGRAMMES = """
            SELECT x.classe_id, cp.cours_id, c.titre, COUNT(DISTINCT cp.id),
                   MIN(CASE WHEN cp.date_cours_prevue >= :maintenant
                             AND cp.etat_cours_programme NOT IN ('ANNULE', 'TERMINE')
                            THEN cp.date_cours_prevue END)
            FROM (SELECT cpc.cours_programmer_id AS cp_id, cpc.classe_id AS classe_id
                  FROM ressources.cours_programmer_classes cpc WHERE cpc.classe_id IN (:classeIds)
                  UNION
                  SELECT cp2.id, cp2.classe_id FROM ressources.cours_programmer cp2 WHERE cp2.classe_id IN (:classeIds)) x
            JOIN ressources.cours_programmer cp ON cp.id = x.cp_id
            JOIN ressources.cours c ON c.id = cp.cours_id
            GROUP BY x.classe_id, cp.cours_id, c.titre
            """;

    List<CoursProg> coursProgrammes(Collection<String> classeIds) {
        if (classeIds.isEmpty()) return List.of();
        MapSqlParameterSource params = new MapSqlParameterSource("classeIds", classeIds)
                .addValue("maintenant", new Timestamp(System.currentTimeMillis()));
        return jdbc.query(SQL_COURS_PROGRAMMES, params, (rs, i) -> new CoursProg(
                rs.getString(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getTimestamp(5)));
    }

    Map<String, Integer> chapitresTotal(Collection<String> coursIds) {
        if (coursIds.isEmpty()) return Map.of();
        Map<String, Integer> out = new HashMap<>();
        jdbc.query("SELECT cours_id, COUNT(*) FROM ressources.chapitres WHERE cours_id IN (:ids) GROUP BY cours_id",
                new MapSqlParameterSource("ids", coursIds), rs -> { out.put(rs.getString(1), rs.getInt(2)); });
        return out;
    }

    /** Chapitres lus par élève et par cours (les deux tables de progression, sans doublon), avec la dernière lecture. */
    private static final String SQL_CHAPITRES_LUS = """
            SELECT x.uid, ch.cours_id, COUNT(DISTINCT x.chapitre_id), MAX(x.dt)
            FROM (SELECT pc.utilisateur_id AS uid, pc.chapitre_id AS chapitre_id, pc.date_completion AS dt
                  FROM ressources.progression_chapitre pc WHERE pc.utilisateur_id IN (:uids)
                  UNION ALL
                  SELECT cpr.user_id, cpr.chapitre_id, cpr.completed_at
                  FROM ressources.chapitre_progress cpr WHERE cpr.completed = TRUE AND cpr.user_id IN (:uids)) x
            JOIN ressources.chapitres ch ON ch.id = x.chapitre_id
            WHERE ch.cours_id IN (:coursIds)
            GROUP BY x.uid, ch.cours_id
            """;

    Map<String, Map<String, Lecture>> chapitresLus(Collection<String> userIds, Collection<String> coursIds) {
        if (userIds.isEmpty() || coursIds.isEmpty()) return Map.of();
        Map<String, Map<String, Lecture>> out = new HashMap<>();
        jdbc.query(SQL_CHAPITRES_LUS, new MapSqlParameterSource("uids", userIds).addValue("coursIds", coursIds),
                rs -> {
                    out.computeIfAbsent(rs.getString(1), k -> new HashMap<>())
                            .put(rs.getString(2), new Lecture(rs.getInt(3), rs.getTimestamp(4)));
                });
        return out;
    }

    List<Part> participations(Collection<String> epIds, Collection<String> userIds) {
        if (epIds.isEmpty() || userIds.isEmpty()) return List.of();
        return jdbc.query("""
                        SELECT utilisateur_id, exercise_programmer_id, etat_soumission, note, date_soumission
                        FROM ressources.participer_exo
                        WHERE exercise_programmer_id IN (:epIds) AND utilisateur_id IN (:uids)
                        """,
                new MapSqlParameterSource("epIds", epIds).addValue("uids", userIds),
                (rs, i) -> new Part(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getTimestamp(5)));
    }

    /** Élèves de la classe : accès + profil élève, hors enseignants (droit de publication) de la classe. */
    List<Eleve> elevesDeLaClasse(String classeId) {
        return jdbc.query("""
                        SELECT u.id, u.nom, u.prenom
                        FROM ressources.acceder a
                        JOIN ressources.utilisateurs u ON u.id = a.utilisateur_id
                        WHERE a.classe_id = :classeId
                          AND EXISTS (SELECT 1 FROM ressources.eleves e WHERE e.eleves_id = u.id)
                          AND NOT EXISTS (SELECT 1 FROM ressources.droit_publication d
                                          WHERE d.utilisateur_id = u.id AND d.classe_id = :classeId)
                        ORDER BY u.nom, u.prenom
                        """,
                new MapSqlParameterSource("classeId", classeId),
                (rs, i) -> new Eleve(rs.getString(1), rs.getString(2), rs.getString(3)));
    }

    List<Classe> classesDeLEleve(String eleveId) {
        return jdbc.query("""
                        SELECT c.id, c.nom, c.niveau, c.etat
                        FROM ressources.acceder a JOIN ressources.classes c ON c.id = a.classe_id
                        WHERE a.utilisateur_id = :eleveId
                        ORDER BY c.nom
                        """,
                new MapSqlParameterSource("eleveId", eleveId),
                (rs, i) -> new Classe(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)));
    }

    Map<String, List<String>> matieres(Collection<String> coursIds) {
        if (coursIds.isEmpty()) return Map.of();
        Map<String, List<String>> out = new HashMap<>();
        jdbc.query("""
                        SELECT cm.cours_id, m.nom FROM ressources.cours_matiere cm
                        JOIN ressources.matieres m ON m.id = cm.matiere_id
                        WHERE cm.cours_id IN (:ids) ORDER BY cm.cours_id, m.nom
                        """,
                new MapSqlParameterSource("ids", coursIds),
                rs -> { out.computeIfAbsent(rs.getString(1), k -> new ArrayList<>()).add(rs.getString(2)); });
        return out;
    }

    /** Par exercice source : {nombre de questions, barème total (1 point par question sans points)}. */
    Map<String, int[]> questions(Collection<String> exerciseIds) {
        if (exerciseIds.isEmpty()) return Map.of();
        Map<String, int[]> out = new HashMap<>();
        jdbc.query("""
                        SELECT exercise_id, COUNT(*), SUM(CASE WHEN points > 0 THEN points ELSE 1 END)
                        FROM ressources.questions_reponses WHERE exercise_id IN (:ids) GROUP BY exercise_id
                        """,
                new MapSqlParameterSource("ids", exerciseIds),
                rs -> { out.put(rs.getString(1), new int[]{rs.getInt(2), rs.getInt(3)}); });
        return out;
    }

    // ─── Utilitaires ──────────────────────────────────────────────────────────

    /** Cours de la classe (programmés ∪ rattachés à un exercice), triés par titre : id -> titre. */
    private static LinkedHashMap<String, String> coursDeLaClasse(String classeId, List<CoursProg> cps, List<Exo> exos) {
        Map<String, String> tous = new HashMap<>();
        cps.stream().filter(cp -> classeId.equals(cp.classeId())).forEach(cp -> tous.putIfAbsent(cp.coursId(), cp.titre()));
        exos.stream().filter(e -> e.coursId() != null).forEach(e -> tous.putIfAbsent(e.coursId(), e.coursTitre()));
        LinkedHashMap<String, String> out = new LinkedHashMap<>();
        tous.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .forEach(e -> out.put(e.getKey(), e.getValue()));
        return out;
    }

    private static List<Exo> actifs(List<Exo> exos) {
        return exos.stream().filter(e -> !e.annule()).toList();
    }

    /** Un exercice programmé diffusé dans plusieurs classes n'est compté qu'une fois. */
    private static List<Exo> dedoublonner(List<Exo> exos) {
        Map<String, Exo> parId = new LinkedHashMap<>();
        exos.forEach(e -> parId.putIfAbsent(e.id(), e));
        return new ArrayList<>(parId.values());
    }

    private static Set<String> ids(List<Exo> exos) {
        return exos.stream().map(Exo::id).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    static String statut(Exo e, Part p, long now) {
        if (p != null && p.corrige()) return "CORRIGE";
        if (p != null && p.rendu()) return "RENDU";
        if (e.echu(now)) return "EN_RETARD";
        return p == null ? "A_FAIRE" : "EN_COURS";
    }

    /**
     * Note ramenée sur 20 : « 15/20 », « 7,5 / 10 », « 2/2 » (-> 20), « 19 » (supposée sur 20). Bornée à [0, 20] ;
     * null si vide ou illisible (ou dénominateur nul, ou note sans barème supérieure à 20).
     */
    public static Double noteSur20(String note) {
        if (note == null) return null;
        String n = note.trim().replace(',', '.').replaceAll("\\s+", "");
        if (n.isEmpty()) return null;
        try {
            double valeur;
            int slash = n.indexOf('/');
            if (slash >= 0) {
                double obtenu = Double.parseDouble(n.substring(0, slash));
                double max = Double.parseDouble(n.substring(slash + 1));
                if (!(max > 0)) return null;
                valeur = obtenu / max * 20.0;
            } else {
                valeur = Double.parseDouble(n);
                if (valeur > 20) return null;
            }
            if (Double.isNaN(valeur) || Double.isInfinite(valeur)) return null;
            return Math.max(0, Math.min(20, valeur));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double moyenne(List<Double> valeurs) {
        return valeurs.isEmpty() ? null : valeurs.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    private static Double round2(Double v) {
        return v == null ? null : Math.round(v * 100.0) / 100.0;
    }

    private static Double round1(Double v) {
        return v == null ? null : Math.round(v * 10.0) / 10.0;
    }

    private static Timestamp max(Timestamp a, Timestamp b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.after(b) ? a : b;
    }

    private static <E extends Enum<E>> E enumOrNull(Class<E> type, String value) {
        if (value == null) return null;
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
